package com.tiesdb.protocol.v0r0.writer;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiesdb.protocol.exception.TiesDBProtocolException;
import com.tiesdb.protocol.v0r0.TiesDBProtocolV0R0.Conversation;
import com.tiesdb.protocol.v0r0.writer.AbstractFunctionWriter.Function;
import com.tiesdb.protocol.v0r0.writer.AbstractFunctionWriter.Function.Argument.ArgumentFunction;
import com.tiesdb.protocol.v0r0.writer.AbstractFunctionWriter.Function.Argument.ArgumentReference;
import com.tiesdb.protocol.v0r0.writer.AbstractFunctionWriter.Function.Argument.ArgumentStatic;
import com.tiesdb.protocol.v0r0.writer.WriterUtil.ConversationConsumer;

import one.utopic.sparse.ebml.format.ASCIIStringFormat;
import one.utopic.sparse.ebml.format.BytesFormat;
import one.utopic.sparse.ebml.format.UTF8StringFormat;

import static com.tiesdb.protocol.v0r0.ebml.TiesDBType.*;
import static com.tiesdb.protocol.v0r0.writer.WriterUtil.write;

public abstract class AbstractFunctionWriter<F extends Function> implements Writer<F> {

    private static final Logger LOG = LoggerFactory.getLogger(RecollectionRequestWriter.class);

    public static interface Function {

        interface Argument {

            interface Visitor<T> {

                T on(ArgumentStatic argumentStatic) throws TiesDBProtocolException;

                T on(ArgumentReference argumentReference) throws TiesDBProtocolException;

                T on(ArgumentFunction argumentFunction) throws TiesDBProtocolException;

            }

            interface ArgumentFunction extends Argument, Function {

                @Override
                default <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
                    return v.on(this);
                }

            }

            interface ArgumentReference extends Argument {

                @Override
                default <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
                    return v.on(this);
                }

                String getName();

            }

            interface ArgumentStatic extends Argument {

                @Override
                default <T> T accept(Visitor<T> v) throws TiesDBProtocolException {
                    return v.on(this);
                }

                String getType();

                byte[] getRawValue();

            }

            <T> T accept(Visitor<T> v) throws TiesDBProtocolException;

        }

        String getName();

        List<Argument> getArguments();

    }

    private final ArgumentWriter argumentWriter;

    public AbstractFunctionWriter(ArgumentWriter argumentWriter) {
        this.argumentWriter = argumentWriter;
    }

    protected ConversationConsumer writeFunction(F function) throws TiesDBProtocolException {
        return write( //
                write(FUNCTION_NAME, UTF8StringFormat.INSTANCE, function.getName()), //
                write(argumentWriter, function.getArguments()) //
        );
    }

    public static class ArgumentWriter implements Writer<Function.Argument> {

        private final ArgumentFunctionWriter argFunctionWriter = new ArgumentFunctionWriter(this);
        private final ArgumentReferenceWriter argReferenceWriter = new ArgumentReferenceWriter();
        private final ArgumentStaticWriter argStaticWriter = new ArgumentStaticWriter();

        @Override
        public void accept(Conversation session, Function.Argument argument) throws TiesDBProtocolException {
            argument.accept(new Function.Argument.Visitor<ConversationConsumer>() {

                @Override
                public ConversationConsumer on(ArgumentStatic argumentStatic) throws TiesDBProtocolException {
                    return write(argStaticWriter, argumentStatic);
                }

                @Override
                public ConversationConsumer on(ArgumentReference argumentReference) throws TiesDBProtocolException {
                    return write(argReferenceWriter, argumentReference);
                }

                @Override
                public ConversationConsumer on(ArgumentFunction argumentFunction) throws TiesDBProtocolException {
                    return write(argFunctionWriter, argumentFunction);
                }

            }).accept(session);
        }

    }

    public static class ArgumentFunctionWriter extends AbstractFunctionWriter<Function.Argument.ArgumentFunction> {

        public ArgumentFunctionWriter(ArgumentWriter argumentWriter) {
            super(argumentWriter);
        }

        @Override
        public void accept(Conversation session, Function.Argument.ArgumentFunction argument) throws TiesDBProtocolException {
            LOG.debug("ArgumentFunction {}", argument);
            write(FUN_ARGUMENT_FUNCTION, //
                    writeFunction(argument)//
            ).accept(session);
        }

    }

    public static class ArgumentReferenceWriter implements Writer<Function.Argument.ArgumentReference> {

        @Override
        public void accept(Conversation session, Function.Argument.ArgumentReference argument) throws TiesDBProtocolException {
            LOG.debug("ArgumentFunction {}", argument);
            write(FUN_ARGUMENT_REFERENCE, ASCIIStringFormat.INSTANCE, argument.getName()).accept(session);
        }

    }

    public static class ArgumentStaticWriter implements Writer<Function.Argument.ArgumentStatic> {

        @Override
        public void accept(Conversation session, Function.Argument.ArgumentStatic argument) throws TiesDBProtocolException {
            LOG.debug("ArgumentStatic {}", argument);
            write(FUN_ARGUMENT_STATIC, //
                    write(ARG_STATIC_TYPE, ASCIIStringFormat.INSTANCE, argument.getType()), //
                    write(ARG_STATIC_VALUE, BytesFormat.INSTANCE, argument.getRawValue()) //
            ).accept(session);
        }

    }
}

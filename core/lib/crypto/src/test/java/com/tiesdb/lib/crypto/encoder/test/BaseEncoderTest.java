package com.tiesdb.lib.crypto.encoder.test;

import static com.tiesdb.lib.crypto.encoder.test.TestVector.vec;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import com.tiesdb.lib.crypto.encoder.EncoderManager;
import com.tiesdb.lib.crypto.encoder.api.Encoder;
import com.tiesdb.lib.crypto.encoder.api.Encoder.ConversionException;

@DisplayName("BaseEncoderTest")
public class BaseEncoderTest {

    // rfc4648 test vectors
    private static final TestVector[] testvec = new TestVector[] { //
            vec(""). // RFC 4648 https://tools.ietf.org/html/rfc4648
                    result(EncoderManager.BASE64, "").//
                    result(EncoderManager.BASE32, ""). //
                    result(EncoderManager.BASE32HEX, ""). //
                    result(EncoderManager.BASE16, ""). //
                    // Packed padding
                    result(EncoderManager.BASE64_PP, "").//
                    result(EncoderManager.BASE32_PP, ""). //
                    result(EncoderManager.BASE32HEX_PP, ""). //
                    // No padding
                    result(EncoderManager.BASE64_NP, "").//
                    result(EncoderManager.BASE32_NP, ""). //
                    result(EncoderManager.BASE32HEX_NP, ""). //
                    end(), //
            vec("f"). // RFC 4648 https://tools.ietf.org/html/rfc4648
                    result(EncoderManager.BASE64, "Zg=="). //
                    result(EncoderManager.BASE32, "MY======"). //
                    result(EncoderManager.BASE32HEX, "CO======"). //
                    result(EncoderManager.BASE16, "66"). //
                    // Packed padding
                    result(EncoderManager.BASE64_PP, "Zg=").//
                    result(EncoderManager.BASE32_PP, "MY==="). //
                    result(EncoderManager.BASE32HEX_PP, "CO==="). //
                    // No padding
                    result(EncoderManager.BASE64_NP, "Zg").//
                    result(EncoderManager.BASE32_NP, "MY"). //
                    result(EncoderManager.BASE32HEX_NP, "CO"). //
                    end(), //
            vec("fo"). // RFC 4648 https://tools.ietf.org/html/rfc4648
                    result(EncoderManager.BASE64, "Zm8="). //
                    result(EncoderManager.BASE32, "MZXQ===="). //
                    result(EncoderManager.BASE32HEX, "CPNG===="). //
                    result(EncoderManager.BASE16, "666F"). //
                    // Packed padding
                    result(EncoderManager.BASE64_PP, "Zm8-").//
                    result(EncoderManager.BASE32_PP, "MZXQ=="). //
                    result(EncoderManager.BASE32HEX_PP, "CPNG=="). //
                    // No padding
                    result(EncoderManager.BASE64_NP, "Zm8").//
                    result(EncoderManager.BASE32_NP, "MZXQ"). //
                    result(EncoderManager.BASE32HEX_NP, "CPNG"). //
                    end(), //
            vec("foo"). // RFC 4648 https://tools.ietf.org/html/rfc4648
                    result(EncoderManager.BASE64, "Zm9v"). //
                    result(EncoderManager.BASE32, "MZXW6==="). //
                    result(EncoderManager.BASE32HEX, "CPNMU==="). //
                    result(EncoderManager.BASE16, "666F6F"). //
                    // Packed padding
                    result(EncoderManager.BASE64_PP, "Zm9v").//
                    result(EncoderManager.BASE32_PP, "MZXW6=-"). //
                    result(EncoderManager.BASE32HEX_PP, "CPNMU=-"). //
                    // No padding
                    result(EncoderManager.BASE64_NP, "Zm9v").//
                    result(EncoderManager.BASE32_NP, "MZXW6"). //
                    result(EncoderManager.BASE32HEX_NP, "CPNMU"). //
                    end(), //
            vec("foob"). // RFC 4648 https://tools.ietf.org/html/rfc4648
                    result(EncoderManager.BASE64, "Zm9vYg=="). //
                    result(EncoderManager.BASE32, "MZXW6YQ="). //
                    result(EncoderManager.BASE32HEX, "CPNMUOG="). //
                    result(EncoderManager.BASE16, "666F6F62"). //
                    // Packed padding
                    result(EncoderManager.BASE64_PP, "Zm9vYg=").//
                    result(EncoderManager.BASE32_PP, "MZXW6YQ-"). //
                    result(EncoderManager.BASE32HEX_PP, "CPNMUOG-"). //
                    // No padding
                    result(EncoderManager.BASE64_NP, "Zm9vYg").//
                    result(EncoderManager.BASE32_NP, "MZXW6YQ"). //
                    result(EncoderManager.BASE32HEX_NP, "CPNMUOG"). //
                    end(), //
            vec("fooba"). // RFC 4648 https://tools.ietf.org/html/rfc4648
                    result(EncoderManager.BASE64, "Zm9vYmE="). //
                    result(EncoderManager.BASE32, "MZXW6YTB"). //
                    result(EncoderManager.BASE32HEX, "CPNMUOJ1"). //
                    result(EncoderManager.BASE16, "666F6F6261"). //
                    // Packed padding
                    result(EncoderManager.BASE64_PP, "Zm9vYmE-").//
                    result(EncoderManager.BASE32_PP, "MZXW6YTB"). //
                    result(EncoderManager.BASE32HEX_PP, "CPNMUOJ1"). //
                    // No padding
                    result(EncoderManager.BASE64_NP, "Zm9vYmE").//
                    result(EncoderManager.BASE32_NP, "MZXW6YTB"). //
                    result(EncoderManager.BASE32HEX_NP, "CPNMUOJ1"). //
                    end(), //
            vec("foobar"). // RFC 4648 https://tools.ietf.org/html/rfc4648
                    result(EncoderManager.BASE64, "Zm9vYmFy"). //
                    result(EncoderManager.BASE32, "MZXW6YTBOI======"). //
                    result(EncoderManager.BASE32HEX, "CPNMUOJ1E8======"). //
                    result(EncoderManager.BASE16, "666F6F626172"). //
                    // Packed padding
                    result(EncoderManager.BASE64_PP, "Zm9vYmFy").//
                    result(EncoderManager.BASE32_PP, "MZXW6YTBOI==="). //
                    result(EncoderManager.BASE32HEX_PP, "CPNMUOJ1E8==="). //
                    // No padding
                    result(EncoderManager.BASE64_NP, "Zm9vYmFy").//
                    result(EncoderManager.BASE32_NP, "MZXW6YTBOI"). //
                    result(EncoderManager.BASE32HEX_NP, "CPNMUOJ1E8"). //
                    end(), //
    };

    @DisplayName("encodeDecodeTest")
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = { //
            EncoderManager.BASE64, //
            EncoderManager.BASE32, //
            EncoderManager.BASE32HEX, //
            EncoderManager.BASE16, //
            EncoderManager.BASE64_PP, //
            EncoderManager.BASE32_PP, //
            EncoderManager.BASE32HEX_PP, //
            EncoderManager.BASE64_NP, //
            EncoderManager.BASE32_NP, //
            EncoderManager.BASE32HEX_NP, //
    })
    public void encodeDecodeTest(String alg) throws ConversionException, IOException {
        Encoder e = EncoderManager.getEncoder(alg);
        try (ByteArrayOutputStream enc = new ByteArrayOutputStream(); ByteArrayOutputStream dec = new ByteArrayOutputStream()) {
            for (TestVector vec : testvec) {
                e.encode(vec.get(), b -> enc.write(b));
                assertTrue(vec.check(alg, enc.toByteArray()));
                e.decode(enc.toByteArray(), b -> dec.write(b));
                assertTrue(vec.check(dec.toByteArray()));
                enc.reset();
                dec.reset();
            }
        }
    }

}

class TestVector {

    private static final Charset TEST_VECTOR_CHARSET = Charset.forName("UTF-8");

    private final byte[] entry;
    private final HashMap<String, byte[]> resultMap = new HashMap<>();

    private TestVector(byte[] entry) {
        this.entry = entry;
    }

    public byte[] get() {
        return Arrays.copyOf(entry, entry.length);
    }

    boolean check(String value) {
        return check(value.getBytes(TEST_VECTOR_CHARSET));
    }

    boolean check(byte[] value) {
        return Arrays.equals(value, entry);
    }

    boolean check(String alg, String value) {
        return check(alg, value.getBytes(TEST_VECTOR_CHARSET));
    }

    boolean check(String alg, byte[] value) {
        return Arrays.equals(value, resultMap.get(alg));
    }

    TestVector result(String alg, String value) {
        return result(alg, value.getBytes(TEST_VECTOR_CHARSET));
    }

    TestVector result(String alg, byte[] value) {
        if (null != resultMap.put(alg, value)) {
            throw new AssertionError("Result was already registered for algorythm " + alg);
        }
        return this;
    }

    TestVector end() {
        return this;
    }

    static TestVector vec(byte[] data) {
        return new TestVector(data);
    }

    static TestVector vec(String string) {
        return vec(string.getBytes(TEST_VECTOR_CHARSET));
    }
}

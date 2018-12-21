/**
 * Copyright © 2017 Ties BV
 *
 * This file is part of Ties.DB project.
 *
 * Ties.DB project is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Ties.DB project is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with Ties.DB project. If not, see <https://www.gnu.org/licenses/lgpl-3.0>.
 */
package network.tiesdb.service.scope.api;

import java.math.BigInteger;

public interface TiesServiceScopeAction {

    interface Distributed extends TiesServiceScopeAction {

        interface ActionConsistency {

            public interface Visitor<T> {

                T on(CountConsistency countConsistency);

                T on(PercentConsistency percentConsistency);

                T on(QuorumConsistency quorumConsistency);

            }

            interface Value {

                Integer getValue();

            }

            interface QuorumConsistency extends ActionConsistency {

                @Override
                default <T> T accept(Visitor<T> v) {
                    return v.on(this);
                }

            }

            interface CountConsistency extends ActionConsistency, Value {

                @Override
                default <T> T accept(Visitor<T> v) {
                    return v.on(this);
                }

            }

            interface PercentConsistency extends ActionConsistency, Value {

                @Override
                default <T> T accept(Visitor<T> v) {
                    return v.on(this);
                }

            }

            <T> T accept(Visitor<T> v);

        }

        ActionConsistency getConsistency();

    }

    BigInteger getMessageId();

}
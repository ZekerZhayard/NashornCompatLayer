/*
 * Copyright (C) 2020-2021  ZekerZhayard
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

package io.github.zekerzhayard.nashorncompatlayer;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class CheckedLambdaUtils {
    public static <A> void wrapConsumer(A a, CheckedConsumer<A> consumer) {
        consumer.accept(a);
    }

    public static <A1, A2> void wrapBiConsumer(A1 a1, A2 a2, CheckedBiConsumer<A1, A2> consumer) {
        consumer.accept(a1, a2);
    }

    public static <A, T> void wrapBiConsumerWithIterable(A a, Iterable<T> iterable, CheckedBiConsumerWithIterable<A, T> consumer) {
        consumer.accept(a, iterable);
    }

    public static <R> R wrapSupplier(CheckedSupplier<R> supplier) {
        return supplier.get();
    }

    public static <A, R> R wrapFunction(A a, CheckedFunction<A, R> supplier) {
        return supplier.apply(a);
    }

    public static <A1, A2, R> R wrapBiFunction(A1 a1, A2 a2, CheckedBiFunction<A1, A2, R> function) {
        return function.apply(a1, a2);
    }

    public static <A1, A2, R> R wrapLazyFunction(A1 a1, CheckedFunction<A1, A2> f, CheckedLazyFunction<A1, A2, R> function) {
        return function.apply(a1, f);
    }

    private static RuntimeException re(Throwable t) {
        return new RuntimeException(t);
    }

    public interface CheckedConsumer<A> extends Consumer<A> {
        @Override
        default void accept(A a) {
            try {
                this.checkedAccept(a);
            } catch (Throwable t) {
                throw re(t);
            }
        }

        void checkedAccept(A a) throws Throwable;
    }

    public interface CheckedBiConsumer<A1, A2> extends BiConsumer<A1, A2> {
        @Override
        default void accept(A1 a1, A2 a2) {
            try {
                this.checkedAccept(a1, a2);
            } catch (Throwable t) {
                throw re(t);
            }
        }

        void checkedAccept(A1 a1, A2 a2) throws Throwable;
    }

    public interface CheckedBiConsumerWithIterable<A, T> extends BiConsumer<A, Iterable<T>> {
        default void accept(A a, Iterable<T> iterable) {
            try {
                for (T t : iterable) {
                    this.checkedAccept(a, t);
                }
            } catch (Throwable t) {
                throw re(t);
            }
        }

        void checkedAccept(A a, T t) throws Throwable;
    }

    public interface CheckedSupplier<R> extends Supplier<R> {
        @Override
        default R get() {
            try {
                return this.checkedGet();
            } catch (Throwable t) {
                throw re(t);
            }
        }

        R checkedGet() throws Throwable;
    }

    public interface CheckedFunction<A, R> extends Function<A, R> {
        @Override
        default R apply(A a) {
            try {
                return this.checkedApply(a);
            } catch (Throwable t) {
                throw re(t);
            }
        }

        R checkedApply(A a) throws Throwable;
    }

    public interface CheckedBiFunction<A1, A2, R> extends BiFunction<A1, A2, R> {
        @Override
        default R apply(A1 a1, A2 a2) {
            try {
                return this.checkedApply(a1, a2);
            } catch (Throwable t) {
                throw re(t);
            }
        }

        R checkedApply(A1 a1, A2 a2) throws Throwable;
    }

    public interface CheckedLazyFunction<A1, A2, R> extends BiFunction<A1, CheckedFunction<A1, A2>, R> {
        @Override
        default R apply(A1 a1, CheckedFunction<A1, A2> f) {
            try {
                return this.checkedApply(a1, f.apply(a1));
            } catch (Throwable t) {
                throw re(t);
            }
        }

        R checkedApply(A1 a1, A2 a2) throws Throwable;
    }
}

package com.ecc.gateway.support;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Objects;
import org.junit.jupiter.api.function.ThrowingSupplier;

public final class Outcome<T> {

    private final T value;
    private final Class<? extends Throwable> exceptionType;
    private final String exceptionMessage;
    private final Throwable cause;

    private Outcome(T value, Class<? extends Throwable> exceptionType, String exceptionMessage, Throwable cause) {
        this.value = value;
        this.exceptionType = exceptionType;
        this.exceptionMessage = exceptionMessage;
        this.cause = cause;
    }

    public static <T> Outcome<T> returned(T value) {
        return new Outcome<>(value, null, null, null);
    }

    public static <T> Outcome<T> thrown(Class<? extends Throwable> exceptionType) {
        return thrown(exceptionType, null);
    }

    public static <T> Outcome<T> thrown(Class<? extends Throwable> exceptionType, String exceptionMessage) {
        return new Outcome<>(null, exceptionType, exceptionMessage, null);
    }

    public static <T> Outcome<T> of(ThrowingSupplier<T> call) {
        try {
            return returned(call.get());
        } catch (Throwable e) {
            return new Outcome<>(null, e.getClass(), e.getMessage(), e);
        }
    }

    @Override
    public boolean equals(Object other) {
        return (
            other instanceof Outcome<?> outcome &&
            Objects.equals(value, outcome.value) &&
            Objects.equals(exceptionType, outcome.exceptionType) &&
            Objects.equals(exceptionMessage, outcome.exceptionMessage)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, exceptionType, exceptionMessage);
    }

    @Override
    public String toString() {
        if (exceptionType == null) {
            return "returned " + value;
        }

        String thrown = "thrown " + exceptionType.getName() + ": " + exceptionMessage;

        if (cause == null) {
            return thrown;
        }

        StringWriter stackTrace = new StringWriter();
        cause.printStackTrace(new PrintWriter(stackTrace));

        return thrown + System.lineSeparator() + stackTrace;
    }
}

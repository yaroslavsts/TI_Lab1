package rsa;

public final class InputValidator {
    public static final long BLOCK_MAX = 255;

    private InputValidator() {
    }

    public static ValidationResult validatePrime(long value, String name) {
        if (value < 2) {
            return ValidationResult.error(name + " должно быть >= 2.");
        }
        if (!MathTools.isPrime(value)) {
            return ValidationResult.error(name + " должно быть простым числом.");
        }
        return ValidationResult.ok();
    }

    public static ValidationResult validateDistinctPrimes(long p, long q) {
        if (p == q) {
            return ValidationResult.error("P и Q должны быть разными простыми числами.");
        }
        return ValidationResult.ok();
    }

    public static ValidationResult validateModulus(long p, long q) {
        long n = p * q;
        if (n <= BLOCK_MAX) {
            return ValidationResult.error(
                "Модуль n = p*q = " + n + " слишком мал. Нужно n > " + BLOCK_MAX + "."
            );
        }
        if (n > 65535) {
            return ValidationResult.error(
                "Модуль n = " + n + " слишком велик для 16-битного блока. Нужно n <= 65535."
            );
        }
        return ValidationResult.ok();
    }

    public static ValidationResult validateKc(long kc, long phi) {
        if (kc <= 1 || kc >= phi) {
            return ValidationResult.error("KC должно быть в диапазоне (1, " + phi + ").");
        }
        if (!MathTools.isRelativelyPrime(kc, phi)) {
            return ValidationResult.error("KC должно быть взаимно простым с phi(n) = " + phi + ".");
        }
        return ValidationResult.ok();
    }
}

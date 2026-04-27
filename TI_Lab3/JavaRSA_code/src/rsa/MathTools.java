package rsa;

public final class MathTools {
    private MathTools() {
    }

    public static long fastExp(long base, long exponent, long mod) {
        long result = 1;
        long currentBase = base % mod;
        long currentExponent = exponent;

        while (currentExponent > 0) {
            if ((currentExponent & 1L) == 1L) {
                result = (result * currentBase) % mod;
            }
            currentExponent >>= 1;
            currentBase = (currentBase * currentBase) % mod;
        }
        return result;
    }

    public static boolean isPrime(long n) {
        if (n <= 1) {
            return false;
        }
        if (n <= 3) {
            return true;
        }
        if (n % 2 == 0 || n % 3 == 0) {
            return false;
        }

        long i = 5;
        while (i * i <= n) {
            if (n % i == 0 || n % (i + 2) == 0) {
                return false;
            }
            i += 6;
        }
        return true;
    }

    public static boolean isRelativelyPrime(long a, long b) {
        return gcd(a, b) == 1;
    }

    public static long modInverse(long a, long m) {
        long[] result = extendedGcd(a, m);
        long gcd = result[0];
        long x = result[1];

        if (gcd != 1) {
            throw new IllegalArgumentException("Обратный элемент не существует");
        }
        return (x % m + m) % m;
    }

    private static long gcd(long a, long b) {
        long x = Math.abs(a);
        long y = Math.abs(b);

        while (y != 0) {
            long temp = y;
            y = x % y;
            x = temp;
        }
        return x;
    }

    private static long[] extendedGcd(long a, long b) {
        if (b == 0) {
            return new long[] {a, 1, 0};
        }

        long[] next = extendedGcd(b, a % b);
        long gcd = next[0];
        long x1 = next[1];
        long y1 = next[2];
        long x = y1;
        long y = x1 - (a / b) * y1;
        return new long[] {gcd, x, y};
    }
}

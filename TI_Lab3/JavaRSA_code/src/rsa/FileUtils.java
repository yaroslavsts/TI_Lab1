package rsa;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class FileUtils {
    private FileUtils() {
    }

    public static byte[] readAllBytes(String path) throws IOException {
        return Files.readAllBytes(Path.of(path));
    }

    public static void writeAllBytes(String path, byte[] data) throws IOException {
        Files.write(Path.of(path), data);
    }

    public static void printAsDecimalBytes(byte[] data) {
        if (data.length == 0) {
            System.out.println("(пусто)");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            sb.append(data[i] & 0xFF);
            if (i < data.length - 1) {
                sb.append(' ');
            }
        }
        System.out.println(sb);
    }

    public static void printAsDecimalUShortBlocks(byte[] data) {
        if (data.length == 0) {
            System.out.println("(пусто)");
            return;
        }
        if (data.length % 2 != 0) {
            System.out.println("Невозможно вывести как 16-битные блоки: нечётная длина данных.");
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < data.length; i += 2) {
            int value = (data[i] & 0xFF) | ((data[i + 1] & 0xFF) << 8);
            sb.append(value);
            if (i < data.length - 2) {
                sb.append(' ');
            }
        }
        System.out.println(sb);
    }
}

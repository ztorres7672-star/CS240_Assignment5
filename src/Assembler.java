import java.io.*;
import java.util.HashMap;
import java.util.Map;
public class Assembler {


    public static void main(String[] args) throws IOException {
        String[] REGISTER_NAMES = {
                "$zero", "$at", "$v0", "$v1", "$a0", "$a1", "$a2", "$a3",
                "$t0", "$t1", "$t2", "$t3", "$t4", "$t5", "$t6", "$t7",
                "$s0", "$s1", "$s2", "$s3", "$s4", "$s5", "$s6", "$s7",
                "$t8", "$t9", "$k0", "$k1", "$gp", "$sp", "$fp", "$ra"
        };
        String[] j_names = {"jal", "j", "jr"};
        String[] i_names = {"addi", "li", "bne", "beq"};
        String[] r_names = {"add", "syscall", "move"};
        BufferedReader reader = new BufferedReader(new FileReader("src/mips4.asm"));
        BufferedWriter testWriter = new BufferedWriter(new FileWriter("cleanfizzbuzz.mhc-ascii"));
        BufferedWriter asciiWriter = new BufferedWriter(new FileWriter("fizzbuzz.mhc-ascii"));
        FileOutputStream binWriter = new FileOutputStream("fizzbuzz.mhc-bin");

        String line;
        int lineNumber = 0;
        Map<String, Integer> labels = new HashMap<>();

        // Example only: use the address where your simulator loads the code.
        final int TEXT_BASE = 0;
        int address = TEXT_BASE;

        boolean inData = false;
        // Example address—must match your assignment's memory layout.
        int dataAddress = 0x10010000;
        ByteArrayOutputStream dataBytes = new ByteArrayOutputStream();

        while ((line = reader.readLine()) != null) {
            lineNumber++;

            // Remove a comment, assuming the source uses # for comments.
            int commentStart = line.indexOf('#');
            if (commentStart != -1) {
                line = line.substring(0, commentStart);
            }

            line = line.trim();

            // Ignore blank lines and lines that contained only comments.
            if (line.isEmpty()) {
                continue;
            }

            if (line.equals(".data")) {
                inData = true;
                continue;
            }
            if (line.equals(".text")) {
                inData = false;
                continue;
            }

            int colon = line.indexOf(':');
            if (colon != -1) {
                String label = line.substring(0, colon).trim();
                labels.put(label, inData ? dataAddress : address);
                line = line.substring(colon + 1).trim();

                if (line.isEmpty()) {continue;}
            }

            String directive = line.split("\\s+", 2)[0];
            if (directive.equals(".asciiz")) {
                String quoted = line.substring(".asciiz".length()).trim();
                if (quoted.length() < 2
                        || !quoted.startsWith("\"")
                        || !quoted.endsWith("\"")) {
                    throw new IOException("Expected a quoted string: " + line);
                }
                String value = quoted.substring(1, quoted.length() - 1);
                // Validate before writing. Escape sequences need extra parsing.
                for (int i = 0; i < value.length(); i++) {
                    char c = value.charAt(i);
                    if (c > 127 || c == '\\' || c == '"') {
                        throw new IOException(
                                "Only plain ASCII strings are supported here"
                        );
                    }
                }
                for (int i = 0; i < value.length(); i++) {
                    dataBytes.write(value.charAt(i));
                }
                dataBytes.write(0); // String terminator
                dataAddress += value.length() + 1;
                continue;
            }


            System.out.println(line);
            testWriter.write(line);
            testWriter.newLine();
            address += 4;
        }
        reader.close();
        testWriter.close();

        lineNumber = 0;
        reader = new BufferedReader(new FileReader("cleanfizzbuzz.mhc-ascii"));
        int pc = TEXT_BASE;

        while ((line = reader.readLine()) != null) {
            lineNumber++;
            String[] parts = line.trim().split("[\\s,]+");

            String opcode = parts[0];
            // addi
            if (opcode.equals(i_names[0])){
                int dest_reg = findRegister(parts[1],REGISTER_NAMES);
                int source_reg = findRegister(parts[2],REGISTER_NAMES);
                int immediate = Integer.parseInt(parts[3]);
                int machineCode = (8 << 26) | (source_reg << 21) | (dest_reg << 16) | (immediate & 0xFFFF);
                writeImmediateOps(binWriter,asciiWriter,machineCode);
            }
            // li -> addiu
            if (opcode.equals(i_names[1])){
                int dest_reg = findRegister(parts[1],REGISTER_NAMES);
                int source_reg = 0;
                int immediate;
                if (parts[2].equals("'\\n'")) {
                    immediate = 10; // Newline character
                } else {immediate = Integer.parseInt(parts[2]);}
                int machineCode = (9 << 26) | (source_reg << 21) | (dest_reg << 16) | (immediate & 0xFFFF);
                writeImmediateOps(binWriter,asciiWriter,machineCode);
            }
            // j (jump)
            if (opcode.equals(j_names[1])) {
                Integer target = labels.get(parts[1]);

                int machineCode = (2 << 26) | ((target >>> 2) & 0x03FFFFFF);

                writeImmediateOps(binWriter, asciiWriter, machineCode);
            }
            // bne
            if (opcode.equals(i_names[2])) {
                int dest_reg = findRegister(parts[1],REGISTER_NAMES);
                int source_reg = findRegister(parts[2],REGISTER_NAMES);
                Integer target = labels.get(parts[3]);

                int offset = (target - (pc + 4)) / 4;

                int machineCode = (5 << 26) | (dest_reg << 21) | (source_reg << 16) | (offset & 0xFFFF);
                writeImmediateOps(binWriter, asciiWriter, machineCode);
            }
            // beq
            if (opcode.equals(i_names[2])) {
                int dest_reg = findRegister(parts[1],REGISTER_NAMES);
                int source_reg = findRegister(parts[2],REGISTER_NAMES);
                Integer target = labels.get(parts[3]);

                int offset = (target - (pc + 4)) / 4;

                int machineCode = (4 << 26) | (dest_reg << 21) | (source_reg << 16) | (offset & 0xFFFF);
                writeImmediateOps(binWriter, asciiWriter, machineCode);
            }
            //syscall
            if (opcode.equals(r_names[1])){
                int machineCode = 12;
                writeImmediateOps(binWriter, asciiWriter, machineCode);
            }
            //move
            if (opcode.equals(r_names[2])){
                int dest_reg = findRegister(parts[1],REGISTER_NAMES);
                int source_reg = findRegister(parts[2],REGISTER_NAMES);
                int machineCode = (0 << 26) | (source_reg << 21) | (dest_reg << 11) | (33 & 0xFFFF);
                writeImmediateOps(binWriter, asciiWriter, machineCode);
            }
            //rem
            if (opcode.equals("rem")) {
                int dest_reg = findRegister(parts[1], REGISTER_NAMES);
                int source_reg = findRegister(parts[2], REGISTER_NAMES);
                int div_reg = findRegister(parts[3], REGISTER_NAMES);

                // div
                int divideCode = (source_reg << 21) | (div_reg << 16) | 26;
                // mfhi
                int remainderCode = (dest_reg << 11) | 16;

                writeImmediateOps(binWriter, asciiWriter, divideCode);
                writeImmediateOps(binWriter, asciiWriter, remainderCode);
            }
            if (opcode.equals("la")) {
                int dest_reg = findRegister(parts[1], REGISTER_NAMES);
                Integer target = labels.get(parts[2]);

                // lui
                int upperCode = (15 << 26) | (dest_reg << 16) | ((target >>> 16) & 0xFFFF);
                // ori
                int lowerCode = (13 << 26) | (dest_reg << 21) | (dest_reg << 16) | (target & 0xFFFF);

                writeImmediateOps(binWriter, asciiWriter, upperCode);
                writeImmediateOps(binWriter, asciiWriter, lowerCode);
            }

            if (opcode.equals("rem") || opcode.equals("la")) {
                pc += 8; // Our implementations write two instructions.
            } else {
                pc += 4; // Other supported cases write one instruction.
            }
        }

        reader.close();
        asciiWriter.close();
        binWriter.close();

    }
    public static void writeImmediateOps(FileOutputStream a, BufferedWriter b, int m) throws IOException {
        a.write((m >>> 24) & 0xFF);
        a.write((m >>> 16) & 0xFF);
        a.write((m >>> 8) & 0xFF);
        a.write(m & 0xFF);

        String bits = String.format(
                "%32s", Integer.toBinaryString(m)
        ).replace(' ', '0');

        b.write(bits);
        b.newLine();
    }
    public static int findRegister(String name, String[] rn) throws IOException {
        for (int i = 0; i < rn.length; i++) {
            if (name.equals(rn[i])) {
                return i;
            }
        }
        throw new IOException("Unknown register: " + name);
    }
}

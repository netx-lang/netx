package pascal.hardscript.util.print;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * A decorator of {@link Writer} that provides a notepad view of a provided writer.
 *
 * <p>It typically provides APIs to manipulate indents so that we can
 * print code conveniently just like writing code on a notepad.</p>
 */
public class NotepadWriter implements AutoCloseable {

    private static final String DEFAULT_INDENT = " ".repeat(2);

    private final Writer writer;

    private final PrintWriter printWriter;

    private final String indentStr;

    private int nIndent = 0;

    private boolean emptyLine = true;

    /**
     * Constructs a NotepadWriter instance.
     * By default, the autoFlush behavior is set to false.
     *
     * @param writer The underlying Writer to which text will be written.
     * @see #NotepadWriter(Writer, boolean)
     */
    public NotepadWriter(Writer writer) {
        this(writer, false);
    }

    /**
     * Constructs a NotepadWriter instance.
     * By default, the indentation is set to four-spaces.
     *
     * @param writer    The underlying Writer to which text will be written.
     * @param autoFlush If true, the PrintWriter will be set to automatically flush
     *                  after every write operation.
     * @see #NotepadWriter(Writer, boolean, String)
     */
    public NotepadWriter(Writer writer, boolean autoFlush) {
        this(writer, autoFlush, DEFAULT_INDENT);
    }

    /**
     * Constructs a NotepadWriter instance.
     *
     * @param writer    The underlying Writer to which text will be written.
     * @param autoFlush If true, the PrintWriter will be set to automatically flush
     *                  after every write operation.
     * @param indentStr The string used for indentation when formatting output.
     *                  Must not be null.
     */
    public NotepadWriter(Writer writer, boolean autoFlush, String indentStr) {
        this.writer = writer;
        this.printWriter = new PrintWriter(writer, autoFlush);
        this.indentStr = Objects.requireNonNull(indentStr);
    }

    /**
     * {@return a Notepad that prints all text to a string}
     * The string can be obtained by call {@link NotepadWriter#toString()}
     * on the returned Notepad.
     */
    public static NotepadWriter strOut() {
        return new NotepadWriter(new StringWriter());
    }

    /**
     * {@return a Notepad that prints all text to stdout}
     */
    public static NotepadWriter stdOut() {
        return new NotepadWriter(new OutputStreamWriter(System.out), true);
    }

    /**
     * {@return a Notepad that prints all text to a file}
     *
     * @param path the path of a file to write.
     * @throws IOException if an I/ O error occurs opening or creating the file
     */
    public static NotepadWriter pathOut(Path path) throws IOException {
        return new NotepadWriter(Files.newBufferedWriter(path));
    }

    /**
     * {@return The Writer to which text is being written}
     */
    public Writer getWriter() {
        return writer;
    }

    /**
     * Increases the current indentation level by one unit.
     * This method is useful for adding nested indentation in formatted
     * nested or hierarchical output in text.
     */
    public void incIndent() {
        nIndent += 1;
    }

    /**
     * Decreases the current indentation level by one unit.
     * This method is used to reduce the indentation depth when formatting
     * nested or hierarchical output in text.
     */
    public void decIndent() {
        nIndent -= 1;
    }

    private void printPotentialIndent() {
        if (emptyLine) {
            printWriter.print(indentStr.repeat(nIndent));
            emptyLine = false;
        }
    }

    /**
     * Prints the provided string to the underlying writer,
     * preceded by the current indentation if a new line is to be printed.
     *
     * @param s The string to be printed. Must not be null.
     */
    public void print(String s) {
        printPotentialIndent();
        printWriter.print(s);
    }

    /**
     * Formats and prints the specified string to the underlying writer using the
     * provided format string and arguments. This method applies the current
     * indentation before printing if a new line is to be printed.
     *
     * @param format a format string as described in
     *               {@link java.util.Formatter#format(String, Object...)}
     * @param args   arguments referenced by the format specifiers in the format string
     */
    public void printf(String format, Object... args) {
        printPotentialIndent();
        printWriter.printf(format, args);
    }

    /**
     * Prints the specified string followed by a newline character.
     * If this is the first output on a line, it will be prefixed with the current indentation.
     *
     * @param s The string to be printed. Must not be null.
     */
    public void println(String s) {
        printPotentialIndent();
        printWriter.println(s);
        emptyLine = true;
    }

    /**
     * Advances the writer to a new line.
     */
    public void newLine() {
        printWriter.println();
        emptyLine = true;
    }

    /**
     * Prints the elements of a list with a specified delimiter, prefix, and suffix,
     * using a provided consumer to print each element.
     *
     * <p>Note that this API is not stable.</p>
     *
     * @param <T> the type of elements to print
     */
    public <T> void printList(List<? extends T> list, Consumer<? super T> printer,
                              String delimiter, String prefix, String suffix) {
        print(prefix);
        for (int i = 0; i < list.size(); ++i) {
            if (i != 0) {
                print(delimiter);
            }
            printer.accept(list.get(i));
        }
        print(suffix);
    }

    /**
     * Flushes the buffered output to the underlying writer.
     */
    public void flush() {
        printWriter.flush();
    }

    /**
     * Close the underlying Writer.
     */
    @Override
    public void close() {
        printWriter.close();
    }

    /**
     * If the underlying Writer is a StringWriter, this method returns the accumulated
     * text content as a single string.
     */
    @Override
    public String toString() {
        return writer.toString();
    }

}

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

/**
 * Word counter that prompts user for a text file and outputs an HTML page with
 * a table of each word and its word count.
 *
 * @author Kevin Le 
 * @author Sean Burns
 *
 */
public final class TagCloud {

    /**
     * No-argument constructor.
     */
    private TagCloud() {

    }

    /**
     * Compare {@code String} keys from {@code Entry}s in alphabetical order.
     */
    private static class AlphabeticalOrder
            implements Comparator<Entry<String, Integer>>, Serializable {

        /**
         * Generated.
         */
        private static final long serialVersionUID = 5430455171526208089L;

        @Override
        public int compare(Entry<String, Integer> o1,
                Entry<String, Integer> o2) {
            /*
             * Return 0 if both keys are the same and both values are the same;
             * return -1 if o1.key appears first alphabetically OR both keys are
             * the same and o1.value is less than o2.value; return 1 if o2.key
             * appears first alphabetically OR both keys are the same and
             * o2.value is less than o1.value
             */
            int result = o1.getKey().compareTo(o2.getKey());
            if (result == 0) {
                result = o1.getValue().compareTo(o2.getValue());
            }
            return result;
        }
    }

    /**
     * Compare {@code Integer} values of {@code Entry}s.
     */
    private static class DecreasingIntegers
            implements Comparator<Entry<String, Integer>>, Serializable {

        /**
         * Generated.
         */
        private static final long serialVersionUID = -7862811225624347967L;

        @Override
        public int compare(Entry<String, Integer> o1,
                Entry<String, Integer> o2) {
            int compared = o2.getValue().compareTo(o1.getValue());
            if (compared == 0) {
                compared = o1.getKey().compareTo(o2.getKey());
            }
            return compared;
        }
    }

    /**
     * Generates the set of characters in the given {@code String} into the
     * given {@code Set}.
     *
     * @param str
     *            the given {@code String}
     * @param charSet
     *            the {@code Set} to be replaced
     * @replaces charSet
     * @ensures charSet = entries(str)
     */
    private static void generateSet(String str, Set<Character> charSet) {
        assert str != null : "Violation of: str is not null";
        assert charSet != null : "Violation of: charSet is not null";

        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!charSet.contains(c)) {
                charSet.add(c);
            }
        }

    }

    /**
     * Returns the first "word" (maximal length string of characters not in
     * {@code separators}) or "separator string" (maximal length string of
     * characters in {@code separators}) in the given {@code text} starting at
     * the given {@code position}.
     *
     * @param text
     *            the {@code String} from which to get the word or separator
     *            string
     * @param position
     *            the starting index
     * @param separators
     *            the {@code Set} of separator characters
     * @return the first word or separator string found in {@code text} starting
     *         at index {@code position}
     * @requires 0 <= position < |text|
     * @ensures <pre>
     * nextWordOrSeparator =
     *   text[position, position + |nextWordOrSeparator|)  and
     * if entries(text[position, position + 1)) intersection separators = {}
     * then
     *   entries(nextWordOrSeparator) intersection separators = {}  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      intersection separators /= {})
     * else
     *   entries(nextWordOrSeparator) is subset of separators  and
     *   (position + |nextWordOrSeparator| = |text|  or
     *    entries(text[position, position + |nextWordOrSeparator| + 1))
     *      is not subset of separators)
     * </pre>
     */
    private static String nextWordOrSeparator(String text, int position,
            Set<Character> separators) {
        assert text != null : "Violation of: text is not null";
        assert separators != null : "Violation of: separators is not null";
        assert 0 <= position : "Violation of: 0 <= position";
        assert position < text.length() : "Violation of: position < |text|";

        int i = position;
        boolean isSep = separators.contains(text.charAt(i));
        while (i < text.length()
                && isSep == separators.contains(text.charAt(i))) {
            i++;
        }

        return text.substring(position, i);

    }

    /**
     * Determines if {@code s} contains any characters that are not digits.
     *
     * @param s
     *            the string to judge
     * @return true if all characters within {@code s} are digits, false
     *         otherwise
     */
    private static boolean allDigits(String s) {
        char[] array = s.toCharArray();
        boolean allDigits = true;
        for (char c : array) {
            if (!(c == '0' || c == '1' || c == '2' || c == '3' || c == '4'
                    || c == '5' || c == '6' || c == '7' || c == '8'
                    || c == '9')) {
                allDigits = false;
            }
        }
        if (s.length() == 0) {
            // I deleted the "or s == null" because there was a warning
            allDigits = false;
        }
        return allDigits;
    }

    /**
     * Returns a HashMap of all words counted in the buffered reader with the
     * amount of times they show up in the file.
     *
     * @param in
     *            input file reader
     * @return map of words with values
     */
    private static HashMap<String, Integer> countWords(BufferedReader in) {

        HashMap<String, Integer> wordCounts = new HashMap<>();
        Set<Character> separators = new TreeSet<>();
        generateSet("\" \t\n\r,-.!?[]';:/()", separators);

        /* Runs through each line in the input file */
        String line = "";
        try {
            line = in.readLine();
        } catch (IOException e) {
            System.err.println("Error reading from input file");
        }
        while (line != null) {
            /* Parses through each word in the line */
            line = line.toLowerCase();
            int position = 0;
            while (position < line.length()) {
                String token = nextWordOrSeparator(line, position, separators);
                if (!separators.contains(token.charAt(0))) {
                    if (wordCounts.containsKey(token)) {
                        /* Adds 1 to word's value */
                        wordCounts.replace(token, wordCounts.get(token) + 1);
                    } else {
                        /* Adds token to queue and map with default value 1 */
                        wordCounts.put(token, 1);
                    }
                }
                position += token.length();
            }
            try {
                line = in.readLine();
            } catch (IOException e) {
                System.err.println("Error reading from input file");
            }
        }

        return wordCounts;

    }

    /**
     * Sorts through the words in the input file and prints the {@code num}
     * words with the highest counts in alphabetical order in the output file in
     * html format.
     *
     * @param in
     *            the input file reader
     * @param out
     *            the output file writer
     * @param inputName
     *            the name of the input file
     * @param num
     *            the number of words requested
     */
    private static void createBody(BufferedReader in, PrintWriter out,
            String inputName, int num) {
        final int eleven = 11;
        final int thirtySeven = 37;
        int localNum = num;

        HashMap<String, Integer> wordCounts = countWords(in);

        /* Sorts map with the most frequent words */
        ArrayList<Entry<String, Integer>> sortByCount = new ArrayList<>();
        Set<Entry<String, Integer>> entrySet = wordCounts.entrySet();
        for (Entry<String, Integer> pair : entrySet) {
            sortByCount.add(pair);
        }

        /* Use decreasing integer comparator to sort */
        sortByCount.sort(new DecreasingIntegers());

        /* Sorts map into alphabetical order */
        ArrayList<Entry<String, Integer>> alphabetical = new ArrayList<>();

        /*
         * Checks if user is asking for more words than are available to print.
         * If there aren't enough words, print a message to the console and
         * change localNum to the size of alphabetical.
         */
        if (sortByCount.size() < localNum) {
            System.out.println(localNum + " words were requested, but only "
                    + sortByCount.size() + " are available. Printing the "
                    + sortByCount.size() + " words with the highest count.");
            localNum = sortByCount.size();
        }
        /*
         * Adds the first pair to alphabetical while also getting its value for
         * maxCount. (This will come in useful for adjusting font sizes)
         */
        int maxCount = 0;
        int minCount = 0;
        if (sortByCount.size() > 0) {
            Entry<String, Integer> firstPair = sortByCount.remove(0);
            alphabetical.add(firstPair);
            maxCount = firstPair.getValue();
        }

        /*
         * Adds the middle pairs to alphabetical based on how many words the
         * user requested
         */
        for (int i = 1; i < localNum - 1; i++) {
            alphabetical.add(sortByCount.remove(0));
        }

        /*
         * Adds the last pair (if it exists) to alphabetical while also getting
         * its value for minCount. (This will come in useful for adjusting font
         * sizes)
         */
        if (localNum > 1) { // just in case
            Entry<String, Integer> lastPair = sortByCount.remove(0);
            alphabetical.add(lastPair);
            minCount = lastPair.getValue();
        }

        /* Use alphabetical comparator to sort */
        alphabetical.sort(new AlphabeticalOrder());

        /* Print HTML file */
        out.println("<html><head>");
        out.println("<title>Top " + localNum + " words in " + inputName
                + "</title>");
        out.println(
                "<link href=\"http://web.cse.ohio-state.edu/software/2231/web-"
                        + "sw2/assignments/projects/tag-cloud-generator/data/"
                        + "tagcloud.css\" rel=\"stylesheet\" type=\"text/css\">");
        out.println("</head>");
        out.println("<body> <h2> Top " + localNum + " words in " + inputName
                + "</h2>");
        out.println("<hr> <div class=\"cdiv\"> <p class=\"cbox\">");

        while (alphabetical.size() != 0) {
            Entry<String, Integer> pair = alphabetical.remove(0);
            String word = pair.getKey();
            int count = pair.getValue();

            /*
             * Font sizes range from 11 to 48 (Difference of 37). We collected
             * maximum and minimum frequencies earlier in the method so we can
             * use that to calculate what format class each word gets based on
             * its count
             *
             * Format Class: ((count- minimum) / (maximum - minimum)) * 37 + 11
             */
            int formatClass = (int) ((double) (count - minCount)
                    / (maxCount - minCount) * thirtySeven + eleven);

            out.print("<span style=\"cursor:default\" class=\"f" + formatClass);
            out.print("\" title=\"count: " + count + "\">" + word);
            out.println("</span>");

        }

        out.println("</p></div></body></html>");

    }

    /**
     * Main method that collects user input.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {
        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in));
        System.out.println("Word Counter");
        System.out.println();
        /*
         * Loop until user enters valid input file name to read from
         */
        System.out.println("File Input Name: ");
        System.out.println("example: BeeMovie.txt ");
        String fileInputName = "";
        BufferedReader fileIn = null; //Used to "initialize" fileIn to stop warnings
        boolean validInput = false;
        while (!validInput) {
            try {
                fileInputName = in.readLine();
            } catch (IOException e) {
                System.err
                        .println("Error reading input file name from console");
                //If file name is not valid, then BufferedReader won't be opened
            }
            try {
                fileIn = new BufferedReader(new FileReader(fileInputName));
                validInput = true;
            } catch (IOException e) {
                System.err.println("Error opening input file");
                validInput = false;
            }
        }
        /*
         * Loops until user enters valid number input
         */
        System.out.println("Number of words to be included: ");
        String numOfWords = "";
        validInput = false;
        while (!validInput) {
            try {
                numOfWords = in.readLine();
                // Keeps checking input until positive integer is entered
                // added null check because of warning
                while (numOfWords != null && (!allDigits(numOfWords)
                        || Integer.parseInt(numOfWords) <= 0)) {
                    System.out.println(
                            "Number of words must be an integer greater than 0.");
                    System.out.println("Number of words to be included: ");
                    numOfWords = in.readLine();
                }
                validInput = true;
            } catch (IOException e) {
                System.err
                        .println("Error reading number of words from console");
                validInput = false;
            }
        }
        int numberOfWords = Integer.parseInt(numOfWords);
        /*
         * Loop until user enters valid output file name to read from
         */
        System.out.println("File Output Name: ");
        String fileOutputName = "";
        PrintWriter fileOut = null; //Used to "initialize" fileOut to stop warnings
        validInput = false;
        while (!validInput) {
            try {
                fileOutputName = in.readLine();
            } catch (IOException e) {
                System.err
                        .println("Error reading output file name from console");
                //If file name is not valid, then BufferedWriter won't be opened
            }
            try {
                fileOut = new PrintWriter(
                        new BufferedWriter(new FileWriter(fileOutputName)));
                validInput = true;
            } catch (IOException e) {
                System.err.println("Error opening output file");
                validInput = false;
            }
        }
        /*
         * Create the html page
         */
        /*
         * What if we made a new method that printed the html header and the
         * strings of the file names were parameters? That way we could pass the
         * file reader and writer to createPage and still use the strings in the
         * html header
         */
        /*
         * I was just about to do that and then I realized that localNum gets
         * updated when the wordCount is lower than the requested value, so if
         * we make the header its own method, it wont update the number in the
         * title
         */
        /* I caved and just added another parameter */
        createBody(fileIn, fileOut, fileInputName, numberOfWords);
        /*
         * Close the inputs and outputs
         */
        fileOut.close();
        try {
            fileIn.close();
        } catch (IOException e) {
            System.err.println("Error closing file input");
        }
        try {
            in.close();
        } catch (IOException e) {
            System.err.println("Error closing console input");
        }
    }

}

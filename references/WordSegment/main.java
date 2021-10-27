import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

class main {
public static void main(String[] args) {
    long N = 1024908267229L;
    Map<String, Long> dictionary = new HashMap<String, Long>();
    int maximumDictionaryWordLength = 0;
    /////////////////////////////////////////////////////////////
    try {
        BufferedReader reader = new BufferedReader(new FileReader("./word_freq.txt"));
        String str;
        while ((str = reader.readLine()) != null) {
            String[] parts = str.split(" ");
            String key = parts[0];
            Long count = Long.parseLong(parts[1]);
            dictionary.put(key, count);
            if (key.length() > maximumDictionaryWordLength) {
                maximumDictionaryWordLength = key.length();
            }
        }
        reader.close();
    } catch (IOException e) {

    }
    /////////////////////////////////////////////////////////////
    long maxDictWordLength = maximumDictionaryWordLength;
    String input = "thisisatest";

    int arraySize = (int)Math.min(maxDictWordLength, input.length());
    int arrayWidth = ((input.length() - 1) >> 6) + 1; // /64 bit
    int arrayWidthByte = arrayWidth << 3; //*8 byte

    long[][] segmentedSpaceBits = new long[arraySize][arrayWidth];
    double[] probabilityLogSum = new double[arraySize];
    int circularIndex = -1;

    for (int j = 0; j < input.length(); j++) {
        int spaceUlongIndex = (j - 1) >> 6; // /64 bit
        int arrayCopyByte = Math.min(((spaceUlongIndex + 1) << 3), arrayWidthByte); // *8 byte

        if (j > 0) {
            segmentedSpaceBits[circularIndex][spaceUlongIndex] |= (1L << ((j - 1) & 0x3f)); // %64 bit
            System.out.println(circularIndex + ", " + spaceUlongIndex + ", " + segmentedSpaceBits[circularIndex][spaceUlongIndex] + ", " + (1L << ((j - 1) & 0x3f)));
        }

        int imax = (int)Math.min(input.length() - j, maxDictWordLength);
        for (int i = 1; i <= imax; i++) {
            int destinationIndex = ((i + circularIndex) % arraySize);
            String part1 = input.substring(j, j + i);
            double ProbabilityLogPart1 = 0.0;
            if (dictionary.containsKey(part1)) {
                long wordCount = dictionary.get(part1);
                ProbabilityLogPart1 = (double)Math.log10((double)wordCount / (double)N);
            } else {
                ProbabilityLogPart1 = (double)Math.log10(10.0 / (N * Math.pow(10.0, part1.length())));
            }

            if (j == 0) {
                probabilityLogSum[destinationIndex] = ProbabilityLogPart1;
            } else if ((i == maxDictWordLength) || (probabilityLogSum[destinationIndex] < probabilityLogSum[circularIndex] + ProbabilityLogPart1)) {
                System.arraycopy(segmentedSpaceBits, circularIndex, segmentedSpaceBits, destinationIndex, arrayWidth);
                probabilityLogSum[destinationIndex] = probabilityLogSum[circularIndex] + ProbabilityLogPart1;
            }
        }

        circularIndex++;
        if (circularIndex == arraySize) {
            circularIndex = 0;
        }
    }

    // for (int r = 0; r < arrayWidth; r++) {
    //     for (int c = 0; c < arraySize; c++) {
    //         System.out.println(segmentedSpaceBits[c][r]);
    //     }
    //     System.out.println("---");
    // }

    StringBuilder resultString = new StringBuilder(input.length() * 2);
    int last = -1;
    for (int i = 0; i <= input.length() - 2; i++) {
        if ((segmentedSpaceBits[circularIndex][i >> 6] & (1L << (i & 0x3f))) > 0) {
            resultString.append(input, last + 1, i);
            // System.out.println(last+1 + " , " + i);
            resultString.append(' ');
            last = i;
        }
    }
    resultString.append(input.substring(last + 1));
    /////////////////////////////////////////////////////////////

    System.out.println(resultString.toString());
}
}

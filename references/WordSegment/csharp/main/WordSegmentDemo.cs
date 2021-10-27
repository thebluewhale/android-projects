using System;

class Program
{
    static void Main(string[] args)
    {
        WordSegmentationTM wordSegmentation = new WordSegmentationTM();

        if (!wordSegmentation.LoadDictionary(AppDomain.CurrentDomain.BaseDirectory + "word_freq.txt"))
            Console.WriteLine("Dictionary file not found.");
        else
        {
            string test = "thihsisatest";
            Console.WriteLine("Input : " + test);
            Console.WriteLine("Output: "+wordSegmentation.Segment(test).segmentedString);
        }

        Console.ReadKey();
    }
}

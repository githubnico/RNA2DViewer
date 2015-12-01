import javafx.util.Pair;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * Implementation of the Nussinov algorithm for RNA secondary structure prediction
 * Created by huson on 11/17/15.
 */
public class Nussinov {
    final private String sequence;
    private ArrayList<Pair<Integer,Integer>> secondaryStructure=null;
    private int score=0;
     private int[][] matrix;

    /**
     * constructor
     * @param sequence input RNA sequence
     */
    public Nussinov (String sequence) {
        this.sequence=sequence;
    }

    /**
     * run the algorithm and return the score
     * @return score
     */
    public int apply () {
        if(matrix==null) {
            matrix=fillMatrix(sequence);
            score=matrix[1][getSequence().length()];
        }
        return score;
    }

    /**
     * get the score of the structure
     * @return score
     */
    public int getScore ()
    {
        return score;
    }

    /**
     * gets the computed secondary structure
     * @return secondary structure, as pairs of nucleotides (their indices, to be precise)
     */
    public ArrayList<Pair<Integer,Integer>> getSecondaryStructure() {
        if(matrix==null)
            apply();
        if(secondaryStructure==null) {
            // Report the base pairs:
            secondaryStructure=new ArrayList<>();
            traceBack(secondaryStructure, matrix, 1, getSequence().length());
            secondaryStructure.sort(new Comparator<Pair<Integer,Integer>>() {
                @Override
                public int compare(Pair<Integer, Integer> p1, Pair<Integer, Integer> p2) {
                    if(p1.getKey()<p2.getKey())
                        return -1;
                    else if(p1.getKey()>p2.getKey())
                        return 1;
                    else
                        return p1.getValue().compareTo(p2.getValue());
                }
            });
        }
        return secondaryStructure;
    }

    /**
     * get the sequence
     * @return sequence
     */
    public String getSequence() {
        return sequence;
    }

    /**
     * get the bracket notation for RNA 2D structure
     * @return bracket notation
     */
    public String getBracketNotation () {
        char[] array=new char[getSequence().length()];
        for(int i=0;i<array.length;i++)
            array[i]='.';
        for(Pair<Integer,Integer> pair:getSecondaryStructure()) {
            array[pair.getKey()]='(';
            array[pair.getValue()]=')';
        }
        return new String(array);
    }

    /**
     * construct and fill the DP matrix using the Nussinov algorithm
     */
    private int[][] fillMatrix (String sequence)
    {
        final int[][] matrix=new int[sequence.length()+1][sequence.length()+1];
        // The main recursion:
        for(int len=5;len<=getSequence().length();len++)
        {
            for(int j=len;j<=getSequence().length();j++)
            {
                int i=j-len+1;
                matrix[i][j]=matrix[i+1][j];

                if(matrix[i][j-1]>matrix[i][j])
                    matrix[i][j]=matrix[i][j-1];

                if(matrix[i+1][j-1]+delta(getSequence().charAt(i-1),getSequence().charAt(j-1))>matrix[i][j])
                    matrix[i][j]=matrix[i+1][j-1]+delta(getSequence().charAt(i-1),getSequence().charAt(j-1));
                for(int k=i+1;k<j;k++)
                {
                    if(matrix[i][k]+matrix[k+1][j]>matrix[i][j])
                        matrix[i][j]=matrix[i][k]+matrix[k+1][j];
                }
            }
        }
        return matrix;
    }


    /** recursively reports all base pairs between positions i and j
     *@param matrix the matrix
     *@param i the left position
     *@param j the right position
     */
    private void traceBack(ArrayList<Pair<Integer,Integer>> secondaryStructure, int[][] matrix, int i, int j) {
        if(i<j)
        {
            if(matrix[i+1][j]==matrix[i][j]) {
                traceBack(secondaryStructure,matrix,i+1,j);
            }
            else if(matrix[i][j-1]==matrix[i][j]) {
                traceBack(secondaryStructure,matrix,i,j-1);
            }
            else if(matrix[i+1][j-1]+delta(getSequence().charAt(i-1),getSequence().charAt(j-1))==matrix[i][j]) {
                secondaryStructure.add(new Pair<>(i-1,j-1));
                traceBack(secondaryStructure,matrix,i+1,j-1);
            }
            else for(int k=i+1;k<=j-1;k++) {
                    if(matrix[i][k]+matrix[k+1][j]==matrix[i][j]) {
                        traceBack(secondaryStructure,matrix,k+1,j);
                        traceBack(secondaryStructure,matrix,i,k);
                        break;
                    }
                }
        }
    }

    /**
     * Returns 1, if bases are complementary, otherwise 0
     *@param a first base
     *@param b second base
     *@return 1, if bases are complementary, otherwise 0
     */
    private static int delta (char a,char b)
    {
        switch(a) {
            case 'a':
            case 'A':
                return (b=='u' || b=='U' || b=='t' || b=='T'?1:0);
            case 'u':
            case 'U':
            case 't':
            case 'T':
                return (b=='a' || b=='A'?1:0);
            case 'c':
            case 'C':
                return (b=='g' || b=='G'?1:0);
            case 'g':
            case 'G':
                return (b=='c' || b=='C'?1:0);
            default:
                return 0;

        }
    }

    /**
     * example of usage
     * @param args ignored
     */
    public static void main (String[] args) {
        String sequence="GGGAAGAUAUAAUCCUAAUGAUAUGGUUUGGGAGUUUCUACCAAGAGCCUUAAACUCUUGAUUAUCUUCCCA";
        Nussinov nussinov=new Nussinov(sequence);
        int score=nussinov.apply();
        ArrayList<Pair<Integer,Integer>> secondaryStructure=nussinov.getSecondaryStructure();

        System.err.println("Score:   "+score);
        System.err.println("Structure:");
        for(Pair<Integer,Integer> pair:secondaryStructure) {
            System.err.println(pair.getKey()+"-"+pair.getValue());
        }

        System.err.println(nussinov.getSequence());
        System.err.println(nussinov.getBracketNotation());
    }
}

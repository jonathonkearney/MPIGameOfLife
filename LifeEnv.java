package MPI;

import java.awt.*;
import mpi.MPI;

class LifeEnv extends Canvas {
    // This holds the data structures for the game and computes the currents
    // The update environment
    private int update[][];
    // The current env
    private int current[][];
    // Need to swap the envs over
    private int swap[][];

    // private static final variables are constants
    private static final int POINT_SIZE = 7;
    private static final Color POINT_COLOR = Color.blue;
    // Width and height of environment
    private static final int N = 100;
    private static final int CANVAS_SIZE = 800;


    public LifeEnv() {
        update = new int[N][N];
        current = new int[N][N];

        for (int i = 0; i < N; i++) {
            current[0][i] = 1;
            current[99][i] = 1;
            current[i][99] = 1;
            current[i][0] = 1;
        }



        setSize(CANVAS_SIZE, CANVAS_SIZE);
    }


    public void runOneIteration(){
        //This method is where the processes meet. The MPI methods below act as MPI.Barrier as well so i could
        //safely send all the processes here and they would wait.

        //used to calculate the execution time
        long startTime = System.nanoTime();

        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int[] sendArray = new int[10800];//2700 x4, 27 rows per process.
        int[] localin = new int[2700];
        int later[] = new int[10800];
        int sendsize = 10800/size; //2700 with 4 processes

        if (rank == 0) { //rank 0 splits the array into segments.
            sendArray = sendArrayMaker();
        }

        if (rank==0) { //send the array out
            MPI.COMM_WORLD.Scatter(sendArray, 0, sendsize, MPI.INT, localin, 0, sendsize, MPI.INT, 0);
        }
        else {
            MPI.COMM_WORLD.Scatter(sendArray, 0, sendsize, MPI.INT, localin, 0, sendsize, MPI.INT, 0);
        }

        int finalArray[] = new int[2500];
        int recvSize = 2500;
        finalArray = Calculate(localin); // calculate the ranks chunk


        if (rank == 0) {// gather all of the calculated arrays
            MPI.COMM_WORLD.Gather(finalArray, 0, recvSize, MPI.INT, later, 0, recvSize, MPI.INT, 0);
        }
        else {
            MPI.COMM_WORLD.Gather(finalArray, 0, recvSize, MPI.INT, later, 0, recvSize, MPI.INT, 0);
        }


        if(rank == 0){ //unflatten into 2d array
            int newCount = 0;

            for (int i = 0; i < 100; i++) {
                for (int j = 0; j < 100; j++) {
                    update[i][j] = later[newCount];
                    newCount++;
                    long iters = 10000;
                    do {
                    } while (--iters > 0);
                }
            }
            swap = current; current = update; update = swap;
            repaint();
            //used to show how long one iteration takes
            long endTime = System.nanoTime();
            long duration = (endTime - startTime)/1000000;
            System.out.println("One iteration executes in: " + duration + " miliseconds");
        }

    }

    private int[] sendArrayMaker(){
        // this method splits the original 100x100 array into four. each chunk is 27x100, 25x100 of the original data
        // plus the data from the side of each chunk. this data is put back to back in one array and therefore flattened
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        int[] sendArray = new int[10800];//2700 x4, 27 rows per process.
        int count = 0;
        for (int i = 0; i < size; i++) {
            int sp = i * 25; // "Start Point" for each ranks group of numbers
            int im = (sp + current[0].length - 1) % current[0].length;
            int ip = (sp + 24 + 1) % current[0].length;
            for (int j = 0; j < 100; j++) {
                sendArray[count] = current[im][j];
                count++;
            }
            for (int j = 1; j < 26; j++) {
                for (int k = 0; k < 100; k++) {
                    sendArray[count] = current[j + sp - 1][k];
                    count++;
                }
            }
            for (int j = 0; j < 100; j++) {
                sendArray[count] = current[ip][j];
                count++;
            }
        }

        return sendArray;
    }

    // I made this method after having trouble with a flattened version of the array. Eventually i disregarded it
    // though because the flattened calculation version doesnt require the extra flattening and unflattening
    // that this one requires. So it is less expensive
    private int[] CalculateAs2D(int[] inArray){
        int outArray[] = new int[2500];
        int twoDOutArray[][] = new int[25][100];
        int workingArray[][] = new int[27][100];
        int newCount = 0;

        for (int i = 0; i < 27; i++) {
            for (int j = 0; j < 100; j++) {
                workingArray[i][j] = inArray[newCount];
                newCount++;
            }
        }

        for (int i = 1; i < 26; i++) {
            for (int j = 0; j < 100; j++) {
                int im = (i+N-1) % N;
                int ip = (i+1) % N;
                int jm = (j+N-1) % N;
                int jp = (j+1) % N;
                switch (workingArray[im][jm] + workingArray[im][j] + workingArray[im][jp] + workingArray[i][jm] + workingArray[i][jp] + workingArray[ip][jm] + workingArray[ip][j] + workingArray[ip][jp]) {
                    case 0 :
                    case 1 : twoDOutArray[i-1][j] = 0; break;
                    case 2 : twoDOutArray[i-1][j] = workingArray[i][j]; break;
                    case 3 : twoDOutArray[i-1][j] = 1; break;
                    case 4 :
                    case 5 :
                    case 6 :
                    case 7 :
                    case 8 : twoDOutArray[i-1][j] = 0; break;
                }

            }
        }
        newCount = 0;
        for (int i = 0; i < 25; i++) {
            for (int j = 0; j < 100; j++) {
                outArray[newCount] = twoDOutArray[i][j];
                newCount++;
            }
        }
        return outArray;
    }

    private int[] Calculate(int[] inArray){
        // this method is called by each process. it is a modified version of the original algorithm. this
        // version calculates each cell using a 1D array not a two.
        int outArray[] = new int[2500];

        int chunkSize = 100;
        int totalLength = 2700; // total length of the inArray
        for (int i = 100; i < 2600; i++) { //goes through rows 1-25, ignores 0 and 26

            //the below find the adjacent cells using the 1D array.
            int down;
            int up;
            if( (i)% chunkSize == 0){
                up = i - 1 + chunkSize;
            }
            else{
                up = i - 1;
            }
            if( (i + 1)% chunkSize == 0){
                down = i + 1 - chunkSize;
            }
            else{
                down = i + 1;
            }
            int left = (i + totalLength - chunkSize) % totalLength;
            int right = (i + totalLength + chunkSize) % totalLength;
            int upLeft = (up + totalLength - chunkSize) % totalLength;
            int upRight = (up + totalLength + chunkSize) % totalLength;
            int downLeft = (down + totalLength - chunkSize) % totalLength;
            int downRight = (down + totalLength + chunkSize) % totalLength;

            switch (inArray[upLeft] + inArray[up] + inArray[upRight] + inArray[left] + inArray[right] + inArray[downLeft] + inArray[down] + inArray[downRight]) {
                case 0 :
                case 1 : outArray[i-100] = 0; break;//had to readjust by -100 to counteract starting for loop at 100
                case 2 : outArray[i-100] = inArray[i]; break;
                case 3 : outArray[i-100] = 1; break;
                case 4 :
                case 5 :
                case 6 :
                case 7 :
                case 8 : outArray[i-100] = 0; break;
                }
            }

        return outArray;
    }


    // Draw the points that have value 1
    public void paint(Graphics g) {
        for (int i = 0; i < N; i++) {
            for (int j = 0; j < N; j++) {
                if (current[i][j] == 1) {
                    drawPoint(i, j, 1, g);
                }
            }
        }

        g.setColor(Color.black);
        g.drawRect(0, 0, getWidth()-1, getHeight()-1);
    }

    private void drawPoint(int x, int y, int v, Graphics g) {
        Dimension d = (getSize());
        int mx = d.width * x / N;
        int my = d.height * y / N;
        if (v == 1) {
            g.setColor(POINT_COLOR);
        } else {
            g.setColor(getBackground());
        }
        g.fillOval(mx, my, POINT_SIZE, POINT_SIZE);
    }
}

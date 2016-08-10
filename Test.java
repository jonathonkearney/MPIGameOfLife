package MPI;

import mpi.MPI;

//this class was made to create a complication free environment for making my methods that i would later
//use in the LifeEnv class. The below may not actually be perfectly working code but i thought i would leave it
//in to show my process and development

public class Test {

    public static void main(String[] args) {

        MPI.Init(args);
        int scatterArray[] = new int[100];
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();

        int rank0Array[] = new int[27];// just for printing
        int othersArray[] = new int[27]; // just for printing

        for (int j = 0; j < 100; j++) {
            scatterArray[j] = j;
        }


        if (rank == 0){
            for (int i = 0; i < size; i++) {
                int p = i; // placeholder count of for loop
                p = i *25; // start point for each ranks group of numbers
                int im = (p+scatterArray.length-1) % scatterArray.length;
                int ip = (p+24+1) % scatterArray.length;
                int tempArray[] = new int[27];
                tempArray[0] = scatterArray[im];
                for (int j = 1; j < 26; j++) {
                    tempArray[j] = scatterArray[j+p-1];
                }
                tempArray[26] = scatterArray[ip];
                if (i == 0){
                    rank0Array = tempArray;
                }
                else {
                    MPI.COMM_WORLD.Send(tempArray, 0, tempArray.length, MPI.INT, i, 99);
                    //System.out.println("rank 0 finishes a loop");
                }

            }
        }
        else {
            int recvArray[] = new int[27];
            MPI.COMM_WORLD.Recv(recvArray, 0, 27, MPI.INT, 0, 99);
            othersArray = recvArray;
        }
        if (rank == 0){
            int finalArray[] = new int[100];
        }
        MPI.COMM_WORLD.Barrier();
        if (rank == 0){
            System.out.println("Rank " + rank + " has: ");
            for (int i = 0; i < rank0Array.length; i++) {
                System.out.print(rank0Array[i] + ", ");
            }
            System.out.println(" ");
        }
        MPI.COMM_WORLD.Barrier();
        if (rank == 1){
            System.out.println("Rank " + rank + " has: ");
            for (int i = 0; i < othersArray.length; i++) {
                System.out.print(othersArray[i] + ", ");
            }
            System.out.println(" ");
        }
        MPI.COMM_WORLD.Barrier();
        if (rank == 2){
            System.out.println("Rank " + rank + " has: ");
            for (int i = 0; i < othersArray.length; i++) {
                System.out.print(othersArray[i] + ", ");
            }
            System.out.println(" ");
        }
        MPI.COMM_WORLD.Barrier();
        if (rank == 3){
            System.out.println("Rank " + rank + " has: ");
            for (int i = 0; i < othersArray.length; i++) {
                System.out.print(othersArray[i] + ", ");
            }
            System.out.println(" ");
        }

        MPI.Finalize();

    }

    private void splitIntoQuarters(){

    }
}

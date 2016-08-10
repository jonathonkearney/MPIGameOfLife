package MPI;

import mpi.MPI;

//this class, along with Test was made to create a complication free environment for making my methods that i would later
//use in the LifeEnv class. The below may not actually be perfectly working code but i thought i would leave it
//in to show my process and development

public class Test2 {
    public static void main(String[] args) throws Exception {
        int sendsize;
        int store[] = new int[20];
        int localin[] = new int[10];
        int later[] = new int[20];

        MPI.Init(args);
        int rank = MPI.COMM_WORLD.Rank();
        int size = MPI.COMM_WORLD.Size();
        sendsize = 20/size;

        if (rank==0) {
            System.out.println("Data quantum: " + sendsize);
            for (int i=0;i<20;i++) {
                store[i] = i;
            }
            MPI.COMM_WORLD.Scatter(store, 0, sendsize, MPI.INT, localin, 0, sendsize, MPI.INT, 0);

        }
        else {
            MPI.COMM_WORLD.Scatter(store, 0, sendsize, MPI.INT, localin, 0, sendsize, MPI.INT, 0);
        }


        System.out.println(rank + ": "+ localin[1]);
        for (int i=0;i<sendsize;i++) {
            localin[i] *= 2;
        }
        System.out.println(rank + ": "+ localin[1]);


        if (rank == 0) {
            MPI.COMM_WORLD.Gather(localin, 0, sendsize, MPI.INT, later, 0, sendsize, MPI.INT, 0);
            for (int i=0;i<20;i++) {
                System.out.print(later[i]+" ");
            }
        }
        else {
            MPI.COMM_WORLD.Gather(localin, 0, sendsize, MPI.INT, later, 0, sendsize, MPI.INT, 0);
        }

        MPI.Finalize();

    }
}
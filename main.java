package MPI;

import mpi.MPI;

import javax.swing.*;
import java.awt.*;

public class main {

    public static void main(String[] args) {

        MPI.Init(args);
        int myRank = MPI.COMM_WORLD.Rank();
        int mysize = MPI.COMM_WORLD.Size();
        GameofLife g = new GameofLife();

        //make the first process setup the environment
        if (myRank == 0 ){
            JFrame frame = new JFrame();
            frame.getContentPane().add(g);
            Container c = frame.getContentPane();
            Dimension d = new Dimension(400,400);
            c.setPreferredSize(d);
            frame.pack();
            frame.setResizable(false);
            frame.setVisible(true);
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            g.init();
            MPI.Finalize();
        }
        else{ // the other processes will meet the first process at the runOneIteration method in LifeEnv
            g.init();
        }


    }

}

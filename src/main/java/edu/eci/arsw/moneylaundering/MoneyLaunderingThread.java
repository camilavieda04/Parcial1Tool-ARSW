/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.eci.arsw.moneylaundering;

import java.io.File;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.concurrent.atomic.AtomicInteger;

//import static edu.eci.arsw.moneylaundering.MoneyLaundering.transactionReader;

/**
 *
 * @author sarah.vieda
 */
public class MoneyLaunderingThread extends Thread{
    
    private TransactionReader transactionReader;
    private List<File> transactionFiles;

    MoneyLaunderingThread(List<File> sublista){
        transactionFiles=sublista;
        transactionReader = new TransactionReader();
    }
    
    @Override
    public void run(){
        for (File transactionFile : transactionFiles) {
            List<Transaction> transactions = transactionReader.readTransactionsFromFile(transactionFile);
            for (Transaction transaction : transactions) {
              synchronized(this){
                if(MoneyLaundering.getPausa()){
                    //System.out.println("Estado pausa:"+MoneyLaundering.getPausa());
                    try {
                        this.wait();
                    } catch (InterruptedException ex) {
                        Logger.getLogger(MoneyLaunderingThread.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
              }
                System.out.println("Sigo ejecutando");
                MoneyLaundering.transactionAnalyzer.addTransaction(transaction);
            }
            
         
    }
       MoneyLaundering.amountOfFilesProcessed.incrementAndGet();
    }
    
    
    public synchronized void despausar() {
        this.notify();
    }
      
}

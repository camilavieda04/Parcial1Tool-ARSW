package edu.eci.arsw.moneylaundering;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MoneyLaundering
{
    public static TransactionAnalyzer transactionAnalyzer;
    public static TransactionReader transactionReader;
    private int amountOfFilesTotal;
    public static AtomicInteger amountOfFilesProcessed;
    // Dividir la búsqueda de las cuentas bancarias fraudulentas en exactamente 5 hilos de ejecución
    private int hilo = 5;
    private static ArrayList<MoneyLaunderingThread> lis;
    private static boolean pausar=false;
    
    
    public MoneyLaundering()
    {
        transactionAnalyzer = new TransactionAnalyzer();
        transactionReader = new TransactionReader();
        amountOfFilesProcessed = new AtomicInteger();
    }

    public void processTransactionData()
    {
        amountOfFilesProcessed.set(0);
        List<File> transactionFiles = getTransactionFileList();
        amountOfFilesTotal = transactionFiles.size(); // 22 Archivos 
        //System.out.println("ARCHIVOS"+amountOfFilesTotal);
        int divi = amountOfFilesTotal/hilo;
        //System.out.println("CADA HILO TIENE"+divi);
        int x=0;
        int y=divi;
        lis = new ArrayList<MoneyLaunderingThread>();
        for(int i=0;i<hilo;i++){
            if(i+1==hilo && y<amountOfFilesTotal){
                y=amountOfFilesTotal;
            }
            MoneyLaunderingThread h = new MoneyLaunderingThread(transactionFiles.subList(x, y));
            x = y;
            y += divi;
            //System.out.println("inicio"+x+"fin"+y);
            //System.out.println("fin"+y);
            h.start();
            lis.add(h);
            //System.out.println("lis"+lis);
        }
       
    }

    public List<String> getOffendingAccounts()
    {
        return transactionAnalyzer.listOffendingAccounts();
    }

    private List<File> getTransactionFileList()
    {
        List<File> csvFiles = new ArrayList<>();
        try (Stream<Path> csvFilePaths = Files.walk(Paths.get("src/main/resources/")).filter(path -> path.getFileName().toString().endsWith(".csv"))) {
            csvFiles = csvFilePaths.map(Path::toFile).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return csvFiles;
    }

    public static void main(String[] args)
    {
       // System.out.println(getBanner());
       //System.out.println(getHelp());
        MoneyLaundering moneyLaundering = new MoneyLaundering();
        ArrayList <Thread> listaHilo = new ArrayList<>();
        moneyLaundering.processTransactionData();
        while(true)
        {
            Scanner scanner = new Scanner(System.in);
            String line = scanner.nextLine();
            if(line.contains("exit"))
            {
               System.exit(0);
            }
            if (line.isEmpty()){
                if(pausar==false){
                    pausar=true;
                    //System.out.println("Pausando");
                }
                else if(pausar==true){
                    pausar=false;
                    //System.out.println("Despausando");
                    for(MoneyLaunderingThread h:lis){
                        //System.out.println("entree");
                        //System.out.println("soy h"+h);
                        h.despausar();
                        System.out.println("Despause");
                    }
                }
            }
            String message = "Processed %d out of %d files.\nFound %d suspect accounts:\n%s";
            List<String> offendingAccounts = moneyLaundering.getOffendingAccounts();
            String suspectAccounts = offendingAccounts.stream().reduce("", (s1, s2)-> s1 + "\n"+s2);
            message = String.format(message, moneyLaundering.amountOfFilesProcessed.get(), moneyLaundering.amountOfFilesTotal, offendingAccounts.size(), suspectAccounts);
            System.out.println(message);
        }
    }

    private static String getBanner()
    {
        String banner = "\n";
        try {
            banner = String.join("\n", Files.readAllLines(Paths.get("src/main/resources/banner.ascii")));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return banner;
    }

    private static String getHelp()
    {
        String help = "Type 'exit' to exit the program. Press 'Enter' to get a status update\n";
        return help;
    }
    
    
     public static boolean getPausa(){
        return pausar;
    }
}
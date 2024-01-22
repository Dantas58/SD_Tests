import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

interface ControloEntrada {
    void podeAbrirEntrada() throws InterruptedException;
    void saiuPassageiro();
    void podeFecharEntrada() throws InterruptedException;
    void entrouPassageiro(String bilhete) throws InterruptedException;
    }

public class ControloEntradaC implements ControloEntrada{
    
    private int capacidade;
    private int passageiros;
    private boolean estado;
    private List<String> bilhetes; // nao especificaram necessidade disto mas fiz
    private ReentrantLock l = new ReentrantLock();
    private Condition vazio = l.newCondition();
    private Condition cheio = l.newCondition();
    private Condition espera = l.newCondition();

    public ControloEntradaC(int N){

        this.capacidade = N;
        this.passageiros = 0;
        this.estado = true;
        this.bilhetes = new ArrayList<>();
    }

    public void podeAbrirEntrada() throws InterruptedException{

        l.lock();
        try{

            while(passageiros > 0){
                vazio.wait();
            }
            
            System.out.println("Pode abrir");
            estado = true;
        }
        finally{
            l.unlock();
        }
    }

    public void saiuPassageiro(){

        l.lock();
        try{

            passageiros--;
            if(passageiros == 0){
                vazio.signalAll();
            }
        }
        finally{
            l.unlock();
        }
    }

    public void podeFecharEntrada() throws InterruptedException{

        l.lock();
        try{

            while(passageiros < capacidade){
                cheio.wait();
            }

            System.out.println("Pode fechar");
            estado = false;
        }
        finally{
            l.unlock();
        }
    }

    public void entrouPassageiro(String bilhete) throws InterruptedException{

        l.lock();
        try{

            while(!estado){
                espera.await();
            }
            bilhetes.add(bilhete);
            passageiros++;
            if(passageiros == capacidade){
                cheio.signalAll();
            }
        }
        finally{
            l.unlock();
        }
    }
}

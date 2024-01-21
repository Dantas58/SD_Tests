import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

interface ControloVacinas {
    void pedirParaVacinar() throws InterruptedException;
    void fornecerFrascos(int frascos);
}

public class ControloVacinasC implements ControloVacinas{
    
    private final int NUM = 10;
    private List<Integer> fila;
    private Map<Integer, Integer> ronda_pacientes;
    private int frascos;
    private int ronda;
    private int contador;
    private Map<Integer, Condition> condicoes;
    private ReentrantLock l = new ReentrantLock();

    public ControloVacinasC(){

        this.fila = new ArrayList<>();
        this.frascos = 0;
        this.condicoes = new HashMap<>();
        this.ronda = 0;
        this.contador = 0;
        this.ronda_pacientes = new HashMap<>();
    }

    public void pedirParaVacinar() throws InterruptedException{

        l.lock();
        try{

            int ronda = this.ronda;
            this.contador++;
            if(condicoes.containsKey(ronda)){
               condicoes.put(ronda, l.newCondition()); 
            }
            if(contador == NUM){
                ronda_pacientes.put(ronda, contador);
                this.ronda++;
                contador = 0;
                fila.add(ronda);
                if(ronda_pacientes.size() == 1 && frascos > 0){
                    condicoes.get(ronda).signalAll();
                }
            }

            while(frascos <= 0 || ronda_pacientes.isEmpty()){
                condicoes.get(ronda).await();
            }

            ronda_pacientes.put(ronda, ronda_pacientes.get(ronda) - 1);
            if(ronda_pacientes.get(ronda) == 0){
                frascos--;
                ronda_pacientes.remove(ronda);
                condicoes.remove(ronda);
            }   
        }
        finally{
            l.unlock();
        }
    }

    public void fornecerFrascos(int frascos){

        l.lock();
        try{

            this.frascos += frascos;
            for(int i = 0; i < ronda_pacientes.size() && i < frascos; i++){

                int prox = fila.remove(0);
                condicoes.get(prox).signalAll();
            }
        }
        finally{
            l.unlock();
        }
    }
}

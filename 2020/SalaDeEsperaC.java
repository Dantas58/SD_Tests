import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

interface SalaDeEspera {
    boolean espera(String nome) throws InterruptedException;
    void desiste(String nome);
    List<String> atende(int n) throws InterruptedException;
}

public class SalaDeEsperaC implements SalaDeEspera {
    

    private List<String> fila;
    private Map<String, Condition> condicoes;
    private Map<String, Boolean> estado;
    private ReentrantLock l = new ReentrantLock();
    private Condition chegou_con = l.newCondition();
    
    public SalaDeEsperaC(){

        this.fila = new ArrayList<>();
        this.condicoes = new HashMap<>();
        this.estado = new HashMap<>();
    }

    public boolean espera(String nome) throws InterruptedException{

        l.lock();
        try{

            fila.add(nome);
            if(fila.size() == 1)
                chegou_con.signal();

            estado.put(nome, true);
            Condition c = l.newCondition();
            condicoes.put(nome, c);

            while(condicoes.containsValue(c)){
                c.await();
            }
            
            return estado.remove(nome);
        }
        finally{
            l.unlock();
        }
    }

    public void desiste(String nome){

        l.lock();
        try{

            fila.remove(nome);
            estado.put(nome, false);
            Condition c = condicoes.remove(nome);
            c.signal();
        }
        finally{
            l.unlock();
        }
    }

    public List<String> atende(int n) throws InterruptedException{

        l.lock();
        try{

            while(fila.isEmpty()){
                chegou_con.await();
            }

            List<String> res = new ArrayList<>();

            for(int i = 0; i < n && !fila.isEmpty(); i++){

                String nome = fila.remove(0);
                res.add(nome);
                Condition c = condicoes.remove(nome);
                c.signal();
            }

            return res;
        }
        finally{
            l.unlock();
        }
    }
}

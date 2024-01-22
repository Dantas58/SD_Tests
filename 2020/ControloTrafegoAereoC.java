import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

interface ControloTrafegoAereo {
    int pedirParaDescolar() throws InterruptedException;
    int pedirParaAterrar() throws InterruptedException;
    void descolou(int pista);
    void aterrou(int pista);
}

public class ControloTrafegoAereoC implements ControloTrafegoAereo{
    
    private List<Condition> fila_aterrar;
    private List<Condition> fila_descolar;
    private List<Boolean> estado_pistas;
    private boolean prox; // true atterar false descolar
    private int pistas;
    private ReentrantLock l = new ReentrantLock();

    public ControloTrafegoAereoC(int num_pistas){

        this.pistas = num_pistas;
        this.fila_aterrar = new ArrayList<>();
        this.fila_descolar = new ArrayList<>();
        this.estado_pistas = new ArrayList<>();
        for(int i = 0; i < pistas; i++){
            estado_pistas.add(true);
        }
        this.prox = true;
    }
    
    public int pedirParaDescolar() throws InterruptedException{

        l.lock();
        try{

            Condition c = l.newCondition();
            fila_aterrar.add(c);

            if(prox && fila_descolar.isEmpty())
                prox = false;

            while(!estado_pistas.contains(true) || prox == true){
                c.await();
            }

            fila_aterrar.remove(c);
            int res = estado_pistas.indexOf(true);
            estado_pistas.set(res, false);
            prox = true;
            return res;
        }
        finally{
            l.unlock();
        }
    }

    public int pedirParaAterrar() throws InterruptedException{

        l.lock();
        try{

            Condition c = l.newCondition();
            fila_descolar.add(c);

            if(!prox && fila_aterrar.isEmpty())
                prox = true;

            while(!estado_pistas.contains(true) || prox == false){
                c.await();
            }

            fila_descolar.remove(c);
            int res = estado_pistas.indexOf(true);
            estado_pistas.set(res, false);
            prox = false;
            return res;
        }
        finally{
            l.unlock();
        }
    }

    public void descolou(int pista){    // adicionar if(!fila_aterrar/descolar.isEmpty) antes dos signals

        l.lock();
        try{

            estado_pistas.set(pista, true);
            if(prox || fila_descolar.isEmpty())
                fila_aterrar.get(0).signal();
            else
                fila_descolar.get(0).signal();
        }
        finally{
            l.unlock();
        }
    }

    public void aterrou(int pista){     // adicionar if(!fila_aterrar/descolar.isEmpty) antes dos signals

        l.lock();
        try{

            estado_pistas.set(pista, true);
            if(prox || fila_descolar.isEmpty())
                fila_aterrar.get(0).signal();
            else
                fila_descolar.get(0).signal();
        }
        finally{
            l.unlock();
        }
    }
}

import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

interface Votacao {
    boolean verifica(int identidade);
    int esperaPorCabine() throws InterruptedException;
    void vota(int escolha);
    void desocupaCabine(int i);
    int vencedor() throws InterruptedException; // apenas para a alinea de Valorização
}

public class VotacaoC implements Votacao{
    

    private List<Integer> votadores;
    private List<Boolean> cabines_livres;
    private int cabines;
    private int opcoes;
    private List<Integer> votos;
    private boolean eleicao_ativa;
    private ReentrantLock l = new ReentrantLock();
    private Condition conCabine = l.newCondition();
    private Condition conFinal = l.newCondition();
    private int votadores_ativos;

    public VotacaoC(int c, int o){

        this.votadores = new ArrayList<>();
        this.cabines = c;
        this.opcoes = o;
        this.cabines_livres = new ArrayList<>();
        for(int i = 0; i < cabines; i++){
            cabines_livres.add(true);
        }
        this.votos = new ArrayList<>();
        for(int i = 0; i < opcoes; i++){
            votos.add(0);
        }
        this.eleicao_ativa = true;
        this.votadores_ativos = 0;
    }

    public boolean verifica(int identidade){

        l.lock();
        try{

            if(votadores.contains(identidade) || !eleicao_ativa){
                return false;
            }
            else{
                votadores.add(identidade);
                votadores_ativos++;
                return true;
            }
        }
        finally{
            l.unlock();
        }
    }

    public int esperaPorCabine() throws InterruptedException{

        l.lock();
        try{
            
            while(!cabines_livres.contains(true)){
                conCabine.await();
            }
            
            int res = cabines_livres.indexOf(true);
            // cabines_livres.set(res, false), se for suposto reserva-la, nao especificado
            return res;
        }
        finally{
            l.unlock();
        }
    }

    public void vota(int escolha){

        l.lock();
        try{

            votos.set(escolha, votos.get(escolha) + 1);
            votadores_ativos--;
            if(votadores_ativos == 0 && !eleicao_ativa){
                conFinal.signal();
            }
        }
        finally{
            l.unlock();
        }
    }

    public void desocupaCabine(int i){

        l.lock();
        try{

            cabines_livres.set(i, true);
            conCabine.signal();
        }
        finally{
            l.unlock();
        }
    }

    public int vencedor() throws InterruptedException{

        l.lock();
        try{

            if(eleicao_ativa) eleicao_ativa = false;

            while(votadores_ativos > 0){
                conFinal.await();
            }

            int res = Collections.max(votos);
            return res;
        }
        finally{
            l.unlock();
        }
    }
}

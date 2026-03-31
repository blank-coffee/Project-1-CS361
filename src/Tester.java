public class Tester {

    public record Result(int size, String type, long time){
        public double mills(){ return time / 1_000_000.0; }
    }

    void main(){

    }

    private static void printResult(Result res){
        System.out.printf("Size: 2^%d | Type: %-8s | Time: %.3f ms%n",
                (int)(Math.log(res.size()) / Math.log(2)), res.type(), res.mills());
    }
}
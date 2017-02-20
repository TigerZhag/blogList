package test;
import java.util.Arrays;


public class test{
    public static void main(String[] args){
        Arrays.asList(1,2,3).stream().map(x -> x * x).forEach(System.out::print);
    }
}

/* This is the main class file */
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

public class NLP {
	
	public static void main(String[] args) throws IOException {
         TopicDetectionUtil util = new TopicDetectionUtil("dataset");
         String topic = util.usingNaiveBayes(new File("testset"));
         System.out.println(topic);
         util.buildWeights();
         System.out.println("------------Using Tf-Idf--------");
         String topic1 = util.usingTfIdfs(new File("testset"));
         System.out.println(topic1);
	}
}

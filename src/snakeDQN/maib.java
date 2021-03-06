package snakeDQN;

import java.util.Arrays;
import java.util.Random;

import DQN.NeuralNetwork;


public class maib {
	
	public static double DISCOUNT_FACTOR = 0.995; //a
	public static double learningFactor = 0.01;
	public static double epsilon = 1;
	public static double epsilonMin = 0.05;
	public static double epsilonDecay = 0.99999;
	private static Random random = new Random();
	
	//Q Value -> Q(state, action) = reward + discountFactor * sum of discountfactor^i*reward[i]
	
	static int[] actions = {-1, 0, 1};
	
	public static void main(String[] args)
	{
		Snake snake = new Snake(10,10);
		
		int[] topology = {snake.width*snake.height, 50, 50, 50, 3};
		NeuralNetwork network = new NeuralNetwork(topology, learningFactor, 0);
		for(int i = 0; i < 1000000; i++)
		{
			snake = new Snake(10, 10);
			int step = 0;
			
			while(!snake.isDead() && step < 1000)
			{
				
				if(epsilon > epsilonMin)
				{
					epsilon *= epsilonDecay;
				}
				
				// get s from environment
				double[] state = snake.flattenedSnakeGrid();
				
				// feed forward s as your networks input to get a list of q values as an output
				double[] outputs = network.predict(state);
				
				// get the highest q value from the neetworks output and execute the action for that neuron
				int actionIndex = getHighestNIndex(outputs);

				
				if(Math.random() < epsilon)
					actionIndex = random.nextInt(3);
				
				double qValue = outputs[actionIndex];
				
				// observe the reward r for that action
				double reward = snake.step(actions[actionIndex]);
				
				//record the new state s'
				double[] statePrime = snake.flattenedSnakeGrid();
				
				// feed forward s' as your networks input to get the list of q values as an output
				double[] outputsPrime = network.predict(statePrime);
				
				// get the highest q value from the q values from the previous value which is maxQ(s',a)
				int actionIndexPrime = getHighestNIndex(outputsPrime);
				double qValueMax = outputsPrime[actionIndexPrime];
				
				//Q(s,a) += learningFactor*(r + discountFactor* maxQ(s',a)-Q(s,a)
				qValue += learningFactor*(reward + DISCOUNT_FACTOR * qValueMax - qValue);
				
				//edit the old outputs with the new qValue
				outputs[actionIndex] = qValue;
				
				//backpropagate with inputs and expected outputs
				network.train(state, outputs, false);
				//System.out.println(reward);
				step++;
				
//				System.out.println(reward);
			}
			
			if(snake.score > 1 || i % 10000 == 0)
				System.out.println("episode " + i + " score: " +  snake.score + " epsilon : " + epsilon);
		}
		
		Snake finalGame = new Snake(10, 10);
		
		while(!finalGame.isDead())
		{			
			System.out.println(finalGame.step(actions[getHighestNIndex(network.predict(finalGame.flattenedSnakeGrid()))]));
			System.out.println(finalGame);
		}
		System.out.println(finalGame.step(actions[getHighestNIndex(network.predict(finalGame.flattenedSnakeGrid()))]));
	}
	
	
	public static int getHighestNIndex(double[] outputs)
	{
		double highestQvalue = Double.MIN_VALUE;
		int actionIndex = 0;
		for(int i = 0; i < outputs.length; i++)
		{
			if(highestQvalue < outputs[i])
			{
				highestQvalue = outputs[i];
				actionIndex = i;
			}
		}
		
		return actionIndex;
	}
		
}


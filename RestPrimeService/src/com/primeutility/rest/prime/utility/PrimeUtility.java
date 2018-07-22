package com.primeutility.rest.prime.utility;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class PrimeUtility {

	public static void main(String[] args) {
		System.out.println(PrimeUtility.isPrime("29497513910652490397"));

	}

	public static boolean isPrime(String num) {
		try {
			BigInteger n = new BigInteger(num);
			long top = (long) Math.sqrt(n.doubleValue()) + 1;

			if (isPrime(n)) {
				ExecutorService executor = Executors.newFixedThreadPool(20);
				CompletionService<Boolean> compService = new ExecutorCompletionService<>(executor);
				List<Future<Boolean>> tasks = new ArrayList<Future<Boolean>>();

				long count = 3;

				int thCount = 40;
//				System.out.println(top);
				for (int i = 1; i <= thCount+1; ++i) {
					long first = count;
					long stop = Math.min(top,top / thCount * i);
//					System.out.println("start: " + first + " End: " + stop);
					Callable<Boolean> task = new Callable<Boolean>() {
						@Override
						public Boolean call() throws Exception {
							for (long k = first; k < stop; k = k + 2) {
								if (n.mod(BigInteger.valueOf(k)) == BigInteger.ZERO) {
									return false;
								}
							}
							return true;
						}
					};

					count = stop + 1;
					tasks.add(compService.submit(task));
				}
				for(int i = 0; i < tasks.size(); ++i) {
					Boolean isPrime = compService.take().get();
					if (!isPrime) {
						executor.shutdownNow(); // always reclaim resources
						return false;
					}
				} 
/*
				for (Future<Boolean> future : tasks) {
					Boolean isPrime = future.get();
					if (!isPrime) {
						executor.shutdownNow(); // always reclaim resources
						return false;
					}
				}
*/
				executor.shutdown(); // always reclaim resources
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean isPrime(BigInteger n) {
		return n.isProbablePrime(5);
	}

}

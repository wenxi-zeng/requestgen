package req.rand;

public class ExpGenerator implements RandomGenerator{
	RandomGenerator uniform;
	double lambda;
	int upper;

	public ExpGenerator(double lambda,int upper,RandomGenerator uniform){
		this.lambda=lambda;
		this.uniform=uniform;
		this.upper=upper;
	}

	@Override
	public int nextInt(){
		return (int)(nextDouble()*upper);
	}

	@Override
	public int nextInt(int upper){
		return (int)(nextDouble()*upper);
	}

	@Override
	public double nextDouble(){
		return -Math.log(1.0-uniform.nextDouble())/lambda;
	}

	@Override
	public void setUpper(int upper) {
		this.upper = upper;
	}
}

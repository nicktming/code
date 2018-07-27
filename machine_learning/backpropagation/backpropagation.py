import numpy as np

def sigmoid(x):
	return 1/(1+np.exp(-x))

def derivative_sigmoid(x):
	return np.multiply(1 - sigmoid(x), sigmoid(x))

def cost_function(yo, Y):
	return 1./(2*m) * np.sum(np.square(np.subtract(yo, Y)))

#num of samples and learning rate
m = 10
step = 0.01

# training samples 2 inputs and 2 outputs
X = np.random.rand(m, 2)
Y = np.random.rand(m, 2)

#layer 2
W2 = np.ones((2, 3))
b2 = np.ones((1, 3))
in2 = np.dot(X, W2) + b2
out2 = sigmoid(in2)

#layer 3
W3 = np.ones((3, 2))
b3 = np.ones((1, 2))
in3 = np.dot(out2, W3) + b3
out3 = sigmoid(in3)

#initial cost
cost = cost_function(out3, Y) 
print("start:", cost)

cnt = 0;
while not cost < 0.1 :
	#find derivative of cost function to in2 in layer3
	derivative_c_out3 = np.subtract(out3, Y) / m
	derivative_out3_in3 = derivative_sigmoid(in3)
	derivative_c_in3 = np.multiply(derivative_c_out3, derivative_out3_in3)
	#find derivative of cost function to W3 and b3 in layer3
	dw3 = np.dot(out2.T, derivative_c_in3)
	db3 = np.sum(derivative_c_in3, axis=0)

	#find derivative of cost function to in2 in layer2
	derivative_out2_in2 = derivative_sigmoid(in2)
	derivative_c_in2 = np.multiply(np.dot(derivative_c_in3, W3.T), derivative_out2_in2)
	#find derivative of cost function to W2 and b2 in layer2
	dw2 = np.dot(X.T, derivative_c_in2)
	db2 = np.sum(derivative_c_in2, axis=0)

	#update all variables
	W3 = W3 - step * dw3
	W2 = W2 - step * dw2
	b3 = b3 - step * db3
	b2 = b2 - step * db2

	# forward to get new out3 with X
	in2 = np.dot(X, W2) + b2
	out2 = sigmoid(in2)
	in3 = np.dot(out2, W3) + b3
	out3 = sigmoid(in3)

	# get new cost with new out3 with X
	cost = cost_function(out3, Y)
	if cnt % 100 == 0:
		print("cost:", cost)
	cnt += 1

#output how many times used to minimize cost
print("end:", cost)
print("cnt:", cnt)





import numpy as np

m = 10
step = 0.01

def sigmoid(x):
	return 1/(1+np.exp(-x))

def derivative_sigmoid(x):
	return np.multiply(1 - sigmoid(x), sigmoid(x))

def cost_function(yo, Y):
	return 1./(2*m) * np.sum(np.square(np.subtract(yo, Y)))

#shape 1*3
X = np.ones((m, 3))
Y = np.random.rand(m, 2)

#shape 3*2
W = np.ones((3, 2))

#shape 1*2
y = np.dot(X, W)

#shape 1*2
yo = sigmoid(y)

cost = cost_function(yo, Y)

print("start:", cost)

cnt = 0;

while not cost < 0.1 :
	derivative_c_y = np.subtract(yo, Y) / m

	derivative_yo_y = derivative_sigmoid(y)

	dw = np.dot(X.T, np.multiply(derivative_c_y, derivative_yo_y))

	W = W - step * dw
	y = np.dot(X, W)
	yo = sigmoid(y)
	cost = cost_function(yo, Y)
	cnt += 1

print("end:", cost)
print("cnt:", cnt)



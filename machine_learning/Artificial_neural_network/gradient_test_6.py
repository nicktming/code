import numpy as np 
import matplotlib.pyplot as plt 

m = 20

# init some data 
x_data = np.arange(1, m + 1).reshape([m, 1])
y_data = x_data*3 + 5

# for polt picture
fig = plt.figure()
ax = fig.add_subplot(1,1,1)
ax.scatter(x_data, y_data)
plt.ion()
plt.show()

w1 = 0
b1 = 0
step = 0.01

def cost_function(y_prediction):
	return 1.0/(2 * m) * np.sum(np.square(y_prediction - y_data))

y_prediction = x_data * w1 + b1
ax.plot(x_data, y_prediction, 'black', lw=3)

print("(i, cost_function)")
for i in range(250):
	
	print(i, cost_function(y_prediction))

	derivative_f_w1 = 1.0/m * np.sum(np.multiply(y_prediction - y_data, x_data))
	derivative_f_b1 = 1.0/m * np.sum(y_prediction - y_data)

	w1 = w1 - step * derivative_f_w1
	b1 = b1 - step * derivative_f_b1
	y_prediction = x_data * w1 + b1


	try:
		ax.lines.remove(lines[0])
	except Exception:
		pass

	lines = ax.plot(x_data, y_prediction, 'r-', lw=3)
	plt.pause(0.1)

	

print('w1:', w1, 'b1:', b1)
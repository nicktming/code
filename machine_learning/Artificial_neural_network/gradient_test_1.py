import numpy as np 
import matplotlib.pyplot as plt 

# init some data 
x_data = np.arange(-10, 11).reshape([21, 1])
y_data = np.square(x_data)*3 + x_data * 4 + 5

# for polt picture
fig = plt.figure()
ax = fig.add_subplot(1,1,1)
ax.plot(x_data, y_data, lw=3)
plt.ion()
plt.show()

start_x = 10
step = 0.1
current_x = start_x
current_y = 3 * current_x * current_x + 4 * current_x + 5
print("(loop_count, current_x, current_y)")
for i in range(10):
	print(i, current_x, current_y)
	derivative_f_x = 6 * current_x + 4
	current_x = current_x - step * derivative_f_x
	current_y = 3 * current_x * current_x + 4 * current_x + 5

	ax.scatter(current_x, current_y)
	plt.pause(0.1)


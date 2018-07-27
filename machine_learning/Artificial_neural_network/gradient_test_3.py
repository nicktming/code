import numpy as np 
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D

# init some data 
x_data = np.arange(-4, 4, 0.25)
y_data = np.arange(-4, 4, 0.25)
f_data = np.square(x_data)*3 + np.square(y_data) * 4 + 5
X, Y = np.meshgrid(x_data, y_data)
Z = np.sqrt(f_data)

fig = plt.figure()
ax = Axes3D(fig)
ax.plot_surface(X, Y, Z, rstride=1, cstride=1, cmap='rainbow')
plt.ion()
plt.show()

start_x = 10
start_y = 10
step = 0.01
current_x = start_x
current_y = start_y
current_f = 3 * current_x * current_x + 4 * current_y + 5
print("(loop_count, current_x, current_y, current_f)")
for i in range(100):
	print(i, current_x, current_y, current_f)
	### derivatives of x and y 
	derivative_f_x = 6 * current_x
	derivative_f_y = 8 * current_y
	### update x, y
	current_x = current_x - step * derivative_f_x
	current_y = current_y - step * derivative_f_y
	### current f 
	current_f = 3 * current_x * current_x + 4 * current_y + 5


	ax.scatter(np.meshgrid(current_x), np.meshgrid(current_y), np.sqrt(current_f))
	plt.pause(0.1)



step = 0.1

a = 5
b = 3
c = 2

u = b * c 
v = a + u
J = 3 * v

while not J < 0.1 :
	print("J:", J)
	# derivatives of variables
	derivative_J_v = v 
	derivative_v_a = 1
	derivative_v_u = 1
	derivative_u_b = c
	derivative_u_c = b 

	derivative_J_a = derivative_J_v * derivative_v_a
	derivative_J_b = derivative_J_v * derivative_v_u * derivative_u_b
	derivative_J_c = derivative_J_v * derivative_v_u * derivative_u_c

	#update variables
	a = a - step * derivative_J_a
	b = b - step * derivative_J_b
	c = c - step * derivative_J_c 

	u = b * c
	v = a + u
	J = 3 * v
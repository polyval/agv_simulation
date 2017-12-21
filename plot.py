import sys
import matplotlib.pyplot as plt

strategies = sys.argv[1].split()
avg_waiting_time = list(map(float, sys.argv[2].split()))
max_waiting_time = list(map(float, sys.argv[3].split()))
distance = list(map(float, sys.argv[4].split()))
tasks = list(map(int, sys.argv[5].split()))
filename_suffix = sys.argv[6]
print(filename_suffix)
fig, ax = plt.subplots()

ax.bar(strategies, avg_waiting_time)
ax.set_xlabel('指派规则')
ax.set_title('平均等待时间')
plt.savefig('平均等待时间' + filename_suffix)
plt.cla()

ax.bar(strategies, max_waiting_time)
ax.set_xlabel('指派规则')
ax.set_title('最大等待时间')
plt.savefig('最大等待时间' + filename_suffix)
plt.cla()

ax.bar(strategies, distance)
ax.set_xlabel('指派规则')
ax.set_title('行驶距离')
plt.savefig('行驶距离' + filename_suffix)
plt.cla()

ax.bar(strategies, tasks)
ax.set_xlabel('指派规则')
ax.set_title('完成任务数')
plt.savefig('完成任务数' + filename_suffix)
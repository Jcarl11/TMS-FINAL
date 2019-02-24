--select round(avg(avg_speed), 2), strftime('%m-%d', timestamp) from rawdata group by strftime('%m-%d', timestamp)

select round(avg(count), 2) AVG_SPEED, strftime('%m-%d', timestamp) DATE from rawdata group by strftime('%m-%d', timestamp)
SELECT hd.closed_by closed_by, 
count(*) as noOfTransaction
FROM 
postgreswbm.transactions_header hd
where 
hd.status_id = 2 
and date_trunc('day', hd."closed_date") >= CAST(:startDate AS timestamp)
and date_trunc('day', hd."closed_date") <= CAST(:endDate AS timestamp)
and hd.closed_by is not null
group by hd.closed_by order by hd.closed_by
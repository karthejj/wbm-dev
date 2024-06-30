SELECT
id as transactionId,
hd.closed_by as closed_by
FROM 
postgreswbm.transactions_header hd
where 
hd.status_id = 2 
and date_trunc('day', hd."closed_date") >= CAST(:startDate AS timestamp)
and date_trunc('day', hd."closed_date") <= CAST(:endDate AS timestamp)
and hd.closed_by is not null
order by hd.id
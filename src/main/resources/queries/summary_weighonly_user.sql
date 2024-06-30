SELECT cht.created_by created_by,
count(*) as noOfTransaction,
sum(cht.material_weight) as quantity,
sum(cht.material_price_after_vat) as materialPrice
FROM postgreswbm.child_transaction cht,
postgreswbm.material mat,
postgreswbm.transactions_header hd
where mat.id = cht.material_id 
and cht.transaction_header_id = hd.id 
and mat.id=16
and date_trunc('day', cht."created_date") >= CAST(:startDate AS timestamp)
and date_trunc('day', cht."created_date") <= CAST(:endDate AS timestamp)
and cht.created_by is not null
group by cht.created_by order by cht.created_by
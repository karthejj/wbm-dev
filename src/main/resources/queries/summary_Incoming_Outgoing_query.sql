--summary report query
SELECT mat.material_name as materialName, 
count(*) as noOfTransaction,
sum(cht.material_weight) as quantity,
sum(cht.material_price_after_vat) as materialPrice
FROM postgreswbm.child_transaction cht,
postgreswbm.material mat,
postgreswbm.transactions_header hd
where 
mat.id = cht.material_id 
and cht.transaction_header_id = hd.id 
and hd.transfer_type =:summaryType
and mat.id<>16
and date_trunc('day', "closed_date")>= CAST(:startDate AS timestamp)
and date_trunc('day', "closed_date")<= CAST(:endDate AS timestamp)
group by mat.material_name order by mat.material_name 
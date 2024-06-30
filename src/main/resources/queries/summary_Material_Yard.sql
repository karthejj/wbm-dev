--Materials in Yard
select mat.material_name as materialName, (case when (listOne.qty1-listTwo.qty2) is null then listOne.qty1
				   			when (listOne.qty1-listTwo.qty2) is not null then listOne.qty1-listTwo.qty2
					END) as quantity
from (
	SELECT mat.id as id,
sum(cht.material_weight) as qty1
FROM postgreswbm.child_transaction cht,
postgreswbm.material mat,
postgreswbm.transactions_header hd
where mat.id = cht.material_id 
and cht.transaction_header_id = hd.id and hd.transfer_type = 'INC'
	and date_trunc('day', "closed_date") >= CAST(:startDate AS timestamp)
and date_trunc('day', "closed_date") <= CAST(:endDate AS timestamp)
group by mat.id order by mat.id
) listOne left join
(
	SELECT mat.id as id,
sum(cht.material_weight) as qty2
FROM postgreswbm.material mat 
left join postgreswbm.child_transaction cht on cht.material_id = mat.id 
join postgreswbm.transactions_header hd on (hd.id = cht.transaction_header_id 
and hd.transfer_type = 'OUT'
										   and date_trunc('day', "closed_date") >= CAST(:startDate AS timestamp)
and date_trunc('day', "closed_date") <= CAST(:endDate AS timestamp))
group by mat.id order by mat.id
) listTwo
on listOne.id = listTwo.id
join postgreswbm.material mat on mat.id = listOne.id and mat.id<>16
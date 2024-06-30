select distinct uname as userName 
from (
select
distinct cht.created_by as uname
FROM postgreswbm.child_transaction cht
where cht.created_by is not null
union
select
distinct cht.modified_by as uname
FROM postgreswbm.child_transaction cht
where cht.created_by is not null
) abc where uname is not null
order by uname
 CREATE TABLE calculate_record (
    currency varchar(10),
    rate numeric(16,2),
    price numeric(16,2),
    discount numeric(16,2),
    result numeric(16,2),
    record_time timestamp with time zone DEFAULT ('now'::text)::timestamp
with time zone
);

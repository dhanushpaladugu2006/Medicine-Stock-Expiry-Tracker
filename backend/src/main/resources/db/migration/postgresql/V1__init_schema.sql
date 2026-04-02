create table if not exists branches (
    id uuid primary key,
    name varchar(120) not null,
    code varchar(40) not null unique,
    address varchar(255) not null,
    city varchar(80) not null,
    state varchar(80) not null,
    country varchar(80) not null,
    phone varchar(30),
    email varchar(150),
    active boolean not null default true,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    created_by varchar(150) not null default 'system',
    updated_by varchar(150) not null default 'system'
);

create table if not exists users (
    id uuid primary key,
    full_name varchar(120) not null,
    email varchar(150) not null unique,
    password varchar(255) not null,
    phone varchar(30),
    role varchar(30) not null,
    branch_id uuid,
    active boolean not null default true,
    email_notifications_enabled boolean not null default true,
    sms_notifications_enabled boolean not null default false,
    last_login_at timestamp with time zone,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    created_by varchar(150) not null default 'system',
    updated_by varchar(150) not null default 'system',
    constraint fk_users_branch foreign key (branch_id) references branches(id)
);

create table if not exists medicines (
    id uuid primary key,
    name varchar(150) not null,
    batch_number varchar(80) not null,
    category varchar(80) not null,
    manufacturer varchar(120) not null,
    quantity integer not null,
    reorder_level integer not null default 10,
    price decimal(12,2) not null,
    expiry_date date not null,
    manufacture_date date not null,
    barcode varchar(120),
    image_url varchar(255),
    status varchar(30) not null,
    branch_id uuid not null,
    archived boolean not null default false,
    last_sold_at timestamp with time zone,
    last_restocked_at timestamp with time zone,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    created_by varchar(150) not null default 'system',
    updated_by varchar(150) not null default 'system',
    constraint uk_medicine_branch_batch unique (branch_id, batch_number),
    constraint fk_medicines_branch foreign key (branch_id) references branches(id)
);

create table if not exists stock_transactions (
    id uuid primary key,
    medicine_id uuid not null,
    branch_id uuid not null,
    performed_by uuid,
    type varchar(30) not null,
    quantity_before integer not null,
    quantity_change integer not null,
    quantity_after integer not null,
    reference_note varchar(255),
    unit_price decimal(12,2),
    transaction_date timestamp with time zone not null default current_timestamp,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    created_by varchar(150) not null default 'system',
    updated_by varchar(150) not null default 'system',
    constraint fk_stock_transactions_medicine foreign key (medicine_id) references medicines(id),
    constraint fk_stock_transactions_branch foreign key (branch_id) references branches(id),
    constraint fk_stock_transactions_user foreign key (performed_by) references users(id)
);

create table if not exists notifications (
    id uuid primary key,
    title varchar(180) not null,
    message varchar(2000) not null,
    type varchar(30) not null,
    status varchar(30) not null,
    medicine_id uuid,
    user_id uuid,
    branch_id uuid,
    channel varchar(20) not null,
    recipient varchar(150),
    sent_at timestamp with time zone,
    read_at timestamp with time zone,
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    created_by varchar(150) not null default 'system',
    updated_by varchar(150) not null default 'system',
    constraint fk_notifications_medicine foreign key (medicine_id) references medicines(id),
    constraint fk_notifications_user foreign key (user_id) references users(id),
    constraint fk_notifications_branch foreign key (branch_id) references branches(id)
);

create table if not exists audit_logs (
    id uuid primary key,
    action varchar(30) not null,
    entity_type varchar(80) not null,
    entity_id varchar(80) not null,
    actor_email varchar(150) not null,
    description varchar(1000) not null,
    metadata varchar(2000),
    created_at timestamp with time zone not null default current_timestamp,
    updated_at timestamp with time zone not null default current_timestamp,
    created_by varchar(150) not null default 'system',
    updated_by varchar(150) not null default 'system'
);

create index if not exists idx_medicines_expiry_date on medicines(expiry_date);
create index if not exists idx_medicines_branch_status on medicines(branch_id, status);
create index if not exists idx_stock_transactions_date on stock_transactions(transaction_date);
create index if not exists idx_notifications_user_created on notifications(user_id, created_at);
create index if not exists idx_audit_logs_created on audit_logs(created_at);

insert into branches (id, name, code, address, city, state, country, phone, email, created_by, updated_by)
values ('00000000-0000-0000-0000-000000000001', 'Central Pharmacy', 'CENTRAL', '12 Health Street', 'Mumbai', 'Maharashtra', 'India', '+91-9000000000', 'central@medicinetracker.local', 'system', 'system')
on conflict (code) do nothing;
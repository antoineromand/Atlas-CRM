create table credentials (
    credentials_id uuid primary key not null,
    email varchar(200) not null unique,
    password varchar(200) not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone,
    role_name varchar(50) not null,
    status varchar(32) not null,
    email_verified boolean not null
);

create index idx_credentials_email on credentials (email);

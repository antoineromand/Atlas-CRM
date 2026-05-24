create table profiles (
    profile_id uuid primary key not null,
    credentials_id uuid not null unique,
    first_name varchar(120),
    last_name varchar(120),
    company_name varchar(200),
    siret_number varchar(14),
    vat_number varchar(32),
    billing_email varchar(200),
    billing_address_line1 varchar(255),
    billing_address_line2 varchar(255),
    billing_postal_code varchar(20),
    billing_city varchar(120),
    billing_country varchar(120),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone,
    constraint fk_profiles_credentials
        foreign key (credentials_id)
            references credentials (credentials_id)
            on delete cascade
);

create index idx_profiles_credentials_id on profiles (credentials_id);
create index idx_profiles_siret_number on profiles (siret_number);

create table clients (
    client_id uuid primary key not null,
    account_id uuid not null,
    company_name varchar(200) not null,
    status varchar(32) not null default 'prospect',
    notes text,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone,
    constraint fk_clients_account
        foreign key (account_id)
            references accounts (account_id)
            on delete cascade,
    constraint chk_clients_status
        check (status in ('prospect', 'active', 'inactive', 'archived'))
);

create index idx_clients_account_id on clients (account_id);
create index idx_clients_status on clients (status);
create index idx_clients_company_name on clients (company_name);

create table client_contacts (
    contact_id uuid primary key not null,
    client_id uuid not null,
    first_name varchar(120),
    last_name varchar(120),
    email varchar(200),
    phone varchar(30),
    job_title varchar(120),
    is_primary boolean not null default false,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone,
    constraint fk_client_contacts_client
        foreign key (client_id)
            references clients (client_id)
            on delete cascade
);

create index idx_client_contacts_client_id on client_contacts (client_id);
create index idx_client_contacts_email on client_contacts (email);
create index idx_client_contacts_is_primary on client_contacts (is_primary);
create unique index ux_client_contacts_primary_per_client
    on client_contacts (client_id)
    where is_primary;

create table client_tags (
    tag_id uuid primary key not null,
    account_id uuid not null,
    name varchar(80) not null,
    color varchar(20),
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone,
    constraint fk_client_tags_account
        foreign key (account_id)
            references accounts (account_id)
            on delete cascade,
    constraint uq_client_tags_account_name
        unique (account_id, name)
);

create index idx_client_tags_account_id on client_tags (account_id);
create index idx_client_tags_name on client_tags (name);

create table client_tag_links (
    client_id uuid not null,
    tag_id uuid not null,
    primary key (client_id, tag_id),
    constraint fk_client_tag_links_client
        foreign key (client_id)
            references clients (client_id)
            on delete cascade,
    constraint fk_client_tag_links_tag
        foreign key (tag_id)
            references client_tags (tag_id)
            on delete cascade
);

create index idx_client_tag_links_client_id on client_tag_links (client_id);
create index idx_client_tag_links_tag_id on client_tag_links (tag_id);

create table client_activities (
    activity_id uuid primary key not null,
    client_id uuid not null,
    activity_type varchar(32) not null,
    title varchar(200) not null,
    description text,
    occurred_at timestamp with time zone not null,
    created_at timestamp with time zone not null,
    updated_at timestamp with time zone,
    constraint fk_client_activities_client
        foreign key (client_id)
            references clients (client_id)
            on delete cascade,
    constraint chk_client_activities_type
        check (activity_type in ('call', 'email', 'meeting', 'note', 'task', 'follow_up', 'status_change'))
);

create index idx_client_activities_client_id on client_activities (client_id);
create index idx_client_activities_type on client_activities (activity_type);
create index idx_client_activities_occurred_at on client_activities (occurred_at);

create table missions (
    mission_id uuid primary key not null,
    account_id uuid not null,

    title varchar(200) not null,
    role_in_project varchar(150),
    description text,

    status varchar(32) not null default 'not_started',
    priority varchar(16) not null default 'medium',

    start_date date not null,
    deadline date,

    created_at timestamp with time zone not null,
    updated_at timestamp with time zone,

    constraint fk_missions_account
        foreign key (account_id)
            references accounts (account_id)
            on delete cascade,

    constraint chk_missions_status
        check (status in ('not_started', 'in_progress', 'completed')),

    constraint chk_missions_priority
        check (priority in ('low', 'medium', 'high'))
);

create index idx_missions_account_id on missions (account_id);
create index idx_missions_status on missions (status);
create index idx_missions_priority on missions (priority);
create index idx_missions_deadline on missions (deadline);

alter table missions
    add column if not exists client_id uuid;

create index if not exists idx_missions_client_id on missions (client_id);

alter table missions
    add constraint fk_missions_client
        foreign key (client_id)
            references clients (client_id)
            on delete set null;

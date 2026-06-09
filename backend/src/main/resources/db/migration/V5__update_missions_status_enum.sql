alter table missions
    drop constraint if exists chk_missions_status;

update missions
set status = 'created'
where status = 'not_started';

alter table missions
    alter column status set default 'created';

alter table missions
    add constraint chk_missions_status
        check (status in (
            'created',
            'analysed',
            'planned',
            'started',
            'in_progress',
            'finalized',
            'shipped',
            'completed'
        ));

import { TestBed, fakeAsync, tick } from '@angular/core/testing';
import { NotificationService } from './notification.service';

describe('NotificationService', () => {
  let service: NotificationService;

  beforeEach(() => {
    TestBed.configureTestingModule({});
    service = TestBed.inject(NotificationService);
  });

  afterEach(() => {
    service.clear();
  });

  it('should create success notifications', () => {
    const id = service.success('Account created');

    expect(id).toBe(1);
    expect(service.notifications().length).toBe(1);
    expect(service.notifications()[0]).toEqual(
      jasmine.objectContaining({
        id: 1,
        title: 'Success',
        message: 'Account created',
        variant: 'success',
      })
    );
  });

  it('should dismiss a notification', () => {
    const id = service.info('Keep going');

    service.dismiss(id);

    expect(service.notifications()).toEqual([]);
  });

  it('should auto dismiss after the configured duration', fakeAsync(() => {
    service.show({ message: 'Temporary', duration: 25 });

    expect(service.notifications().length).toBe(1);

    tick(25);

    expect(service.notifications()).toEqual([]);
  }));
});

import { Component } from '@angular/core';
import { FeatureCardComponent } from '../../../../shared/ui/feature-card/feature-card.component';
import { HeroSectionComponent } from '../../components/hero-section/hero-section.component';
import { MarketingHeaderComponent, MarketingLink } from '../../components/marketing-header/marketing-header.component';
import { MarketingFooterComponent } from '../../components/marketing-footer/marketing-footer.component';
import { ScreenCardComponent } from '../../../../shared/ui/screen-card/screen-card.component';
import { SectionHeadingComponent } from '../../../../shared/ui/section-heading/section-heading.component';
import { TestimonialBandComponent, TestimonialItem } from '../../components/testimonial-band/testimonial-band.component';

type LandingTone = 'primary' | 'secondary' | 'accent';

interface LandingFeature {
  icon: string;
  title: string;
  description: string;
  tone: LandingTone;
}

@Component({
  selector: 'app-landing-page-component',
  standalone: true,
  imports: [
    FeatureCardComponent,
    HeroSectionComponent,
    MarketingHeaderComponent,
    MarketingFooterComponent,
    ScreenCardComponent,
    SectionHeadingComponent,
    TestimonialBandComponent,
  ],
  templateUrl: './landing-page-component.html',
  styleUrl: './landing-page-component.scss',
})
export class LandingPageComponent {
  protected readonly navLinks: readonly MarketingLink[] = [
    { label: 'Features', href: '#features' },
    { label: 'Overview', href: '#dashboard' },
    { label: 'Benefits', href: '#benefits' },
  ];

  protected readonly heroStats = [
    { label: 'Time saved', value: '40%', meta: 'on weekly admin work', tone: 'accent' as const },
    { label: 'Centralized clients', value: '120+', meta: 'in one clear, unified view', tone: 'secondary' as const },
    { label: 'Tracked payments', value: '96%', meta: 'with automated follow-ups', tone: 'primary' as const },
  ];

  protected readonly screens = [
    {
      imageSrc: '/atlas/screens/dashboard/screen.png',
      imageAlt: 'Atlas CRM dashboard screenshot',
      title: 'Dashboard',
      description: 'The primary product view, shaped by the Stitch visual hierarchy.',
      frameLabel: 'Dashboard',
    },
    {
      imageSrc: '/atlas/screens/clients/screen.png',
      imageAlt: 'Atlas CRM clients page screenshot',
      title: 'Clients',
      description: 'A clear view of relationships, contacts, and follow-ups.',
      frameLabel: 'Clients',
    },
    {
      imageSrc: '/atlas/screens/missions/screen.png',
      imageAlt: 'Atlas CRM missions page screenshot',
      title: 'Missions',
      description: 'Operational tracking with the same density and rhythm.',
      frameLabel: 'Missions',
    },
    {
      imageSrc: '/atlas/screens/invoices/screen.png',
      imageAlt: 'Atlas CRM invoices and quotes page screenshot',
      title: 'Invoices & quotes',
      description: 'The admin layer, kept visually consistent with everything else.',
      frameLabel: 'Invoices',
    },
  ];

  protected readonly features: readonly LandingFeature[] = [
    {
      icon: 'group',
      title: 'Client management',
      description: 'A smart directory that centralizes contracts, preferences, and every client interaction.',
      tone: 'primary',
    },
    {
      icon: 'receipt_long',
      title: 'Invoicing, simplified',
      description: 'Create clean quotes and invoices, track payments, and reduce manual follow-ups.',
      tone: 'secondary',
    },
    {
      icon: 'assignment',
      title: 'Mission tracking',
      description: 'See project progress clearly, with deadlines always in sight.',
      tone: 'accent',
    },
  ];

  protected readonly testimonials: readonly TestimonialItem[] = [
    {
      quote: 'Atlas changed how I work. I no longer waste time searching for invoices.',
      author: 'Sarah L.',
      role: 'Freelance UI designer',
    },
    {
      quote: 'Clients and missions finally live in an interface that stays readable when things move fast.',
      author: 'Marc D.',
      role: 'Independent consultant',
    },
    {
      quote: 'The visual direction builds trust without clutter or gimmicks.',
      author: 'Aisha R.',
      role: 'Art director',
    },
  ];

  protected readonly benefits: readonly LandingFeature[] = [
    {
      icon: 'speed',
      title: 'Efficiency',
      description: 'Reduce time spent on repetitive tasks and focus on the work that matters.',
      tone: 'primary',
    },
    {
      icon: 'lightbulb',
      title: 'Clarity',
      description: 'A clear view of cash flow, missions, and what needs to happen next.',
      tone: 'secondary',
    },
    {
      icon: 'verified',
      title: 'Professionalism',
      description: 'Polished documents and follow-up that reinforce client trust.',
      tone: 'accent',
    },
  ];
}

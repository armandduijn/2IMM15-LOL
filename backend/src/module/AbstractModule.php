<?php

namespace App;

use Psr\Container\ContainerInterface;

abstract class AbstractModule implements ContainerAwareInterface
{
    /**
     * @var
     */
    private $container;

    /**
     * @param ContainerInterface|null $container
     * @return void
     */
    public function setContainer(ContainerInterface $container = null): void
    {
        $this->container = $container;
    }

    /**
     * @return ContainerInterface
     */
    protected function getContainer(): ContainerInterface
    {
        return $this->container;
    }
}
